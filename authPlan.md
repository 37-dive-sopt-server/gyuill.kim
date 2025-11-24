### Situation :
지금부터 이 프로젝트에 Spring Security를 이용한 회원 정책과 개인정보 처리 방침을 설정하려고 해
###  Task:
로그인, 비밀번호 재설정, 회원 탈퇴 등과 관련된 정책을 수립하고, 개인정보 보호를 위한 방침을 마련해야 해

들어가야 하는 기능은 다음과 같아:
- Access Token
- RefreshToken
  - DB 저장
  - RTR
  - 블랙리스트
  - 일정 시간 지나면 삭제

### Intent :
Spring Security를 활용하여 안전하고 효율적인 회원 관리 시스템을 구축하는 것이 목표야. 이를 위해 Access Token과 Refresh Token의 발급 및 관리를 체계적으로 설계할 필요가 있어.
불필요한 의존성 중복 코드 없이 최대한 컴팩트한 코드를 작성하는 것이 중요해.
계획서에는 실제 구현 예시 코드는 넣지 말아줘
구현할 패키지 구조에 대한 계획, 클래스간의 의존성도 포함해줘
실제 구현이 아닌 계획서를 먼저 작성해줘

---

## JWT 기반 인증/인가 시스템 구현 계획서

### 1. 시스템 아키텍처 개요

#### 1.1 인증 플로우
- **로그인**: 이메일/비밀번호 검증 → Access Token + Refresh Token 발급
- **API 요청**: Access Token 검증 → 인가된 리소스 접근
- **토큰 갱신**: Refresh Token 검증 → 새로운 Access Token + Refresh Token 발급 (RTR)
- **로그아웃**: Refresh Token 블랙리스트 등록 → 토큰 무효화

#### 1.2 토큰 전략
- **Access Token**:
  - 유효기간: 1시간 (3600초)
  - 저장소: 클라이언트 메모리 또는 상태 관리
  - JWT Claim: userId, email, 발급/만료 시간

- **Refresh Token**:
  - 유효기간: 14일 (1,209,600초)
  - 저장소: DB (Redis 또는 MySQL)
  - RTR (Refresh Token Rotation) 적용: 매 갱신 시 새 토큰 발급
  - 블랙리스트: 로그아웃 시 즉시 무효화
  - 자동 삭제: 만료 시간 경과 시 스케줄러로 제거

### 2. 패키지 구조 설계

```
org.sopt/
├── domain/
│   ├── auth/                          # 인증 도메인
│   │   ├── application/
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest      # 로그인 요청 DTO
│   │   │   │   ├── LoginResponse     # 로그인 응답 DTO (Access + Refresh)
│   │   │   │   ├── TokenRefreshRequest  # 토큰 갱신 요청 DTO
│   │   │   │   └── TokenRefreshResponse # 토큰 갱신 응답 DTO
│   │   │   └── service/
│   │   │       └── AuthService       # 인증 비즈니스 로직
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   └── RefreshToken      # Refresh Token 엔티티
│   │   │   └── repository/
│   │   │       └── RefreshTokenRepository  # Refresh Token 저장소
│   │   ├── exception/
│   │   │   └── AuthException         # 인증 관련 예외
│   │   └── presentation/
│   │       └── controller/
│   │           └── AuthController    # 인증 엔드포인트
│   │
│   └── member/
│       └── application/
│           └── service/
│               └── MemberService      # 기존 회원가입 시 비밀번호 암호화 추가
│
└── global/
    ├── auth/                          # 인증/인가 인프라
    │   ├── jwt/
    │   │   ├── JwtProvider           # JWT 생성/검증/파싱
    │   │   ├── JwtProperties         # JWT 설정값 바인딩
    │   │   └── JwtAuthenticationFilter  # JWT 검증 필터
    │   ├── security/
    │   │   ├── SecurityConfig        # Spring Security 설정
    │   │   ├── CustomUserDetails     # UserDetails 구현체
    │   │   └── CustomUserDetailsService  # UserDetailsService 구현체
    │   └── exception/
    │       └── JwtAuthenticationEntryPoint  # 인증 실패 핸들러
    │
    ├── config/
    │   └── PasswordEncoderConfig     # PasswordEncoder 빈 설정
    │
    └── scheduler/
        └── RefreshTokenCleanupScheduler  # 만료된 토큰 자동 삭제
```

### 3. 주요 컴포넌트 설계

#### 3.1 Domain Layer (도메인)

##### 3.1.1 RefreshToken Entity
- **책임**: Refresh Token의 생명주기 관리
- **필드**:
  - `id`: PK (Long)
  - `memberId`: 회원 ID (외래키)
  - `token`: Refresh Token 문자열 (unique, indexed)
  - `expiryDate`: 만료 시간
  - `isBlacklisted`: 블랙리스트 여부 (기본값: false)
  - `createdAt`: 생성 시간
- **메서드**:
  - `isExpired()`: 만료 여부 확인
  - `markAsBlacklisted()`: 블랙리스트 처리
  - `isValid()`: 유효성 검증 (만료되지 않았고 블랙리스트가 아님)

##### 3.1.2 RefreshTokenRepository
- **책임**: Refresh Token 영속성 관리
- **주요 메서드**:
  - `findByToken(String token)`: 토큰으로 조회
  - `findByMemberId(Long memberId)`: 회원별 토큰 조회
  - `deleteByMemberId(Long memberId)`: 회원별 토큰 삭제 (로그아웃)
  - `deleteByExpiryDateBefore(LocalDateTime dateTime)`: 만료된 토큰 일괄 삭제
  - `existsByTokenAndIsBlacklistedFalse(String token)`: 유효한 토큰 존재 여부

##### 3.1.3 AuthService
- **책임**: 인증 비즈니스 로직 처리
- **의존성**:
  - `MemberRepository`: 회원 정보 조회
  - `RefreshTokenRepository`: Refresh Token CRUD
  - `JwtProvider`: JWT 생성/검증
  - `PasswordEncoder`: 비밀번호 검증
- **주요 메서드**:
  - `login(LoginRequest)`: 로그인 (Access + Refresh Token 발급)
  - `refreshToken(TokenRefreshRequest)`: 토큰 갱신 (RTR 적용)
  - `logout(String refreshToken)`: 로그아웃 (블랙리스트 등록)
  - `validateRefreshToken(String token)`: Refresh Token 유효성 검증

##### 3.1.4 AuthController
- **책임**: 인증 관련 API 엔드포인트 제공
- **의존성**: `AuthService`
- **엔드포인트**:
  - `POST /auth/login`: 로그인
  - `POST /auth/refresh`: 토큰 갱신
  - `POST /auth/logout`: 로그아웃
- **응답 형식**: 기존 `CommonApiResponse` 활용

#### 3.2 Infrastructure Layer (인프라)

##### 3.2.1 JwtProvider
- **책임**: JWT 토큰의 생성, 검증, 파싱
- **의존성**: `JwtProperties` (설정값 주입)
- **주요 메서드**:
  - `generateAccessToken(Long userId, String email)`: Access Token 생성
  - `generateRefreshToken(Long userId)`: Refresh Token 생성
  - `validateToken(String token)`: 토큰 유효성 검증 (서명, 만료 시간)
  - `getUserIdFromToken(String token)`: 토큰에서 userId 추출
  - `getEmailFromToken(String token)`: 토큰에서 email 추출
- **예외 처리**:
  - 만료된 토큰
  - 잘못된 서명
  - 파싱 실패

##### 3.2.2 JwtProperties
- **책임**: application.yml의 JWT 설정값 바인딩
- **어노테이션**: `@ConfigurationProperties(prefix = "security.jwt")`
- **필드**:
  - `secret`: JWT 서명 키
  - `expiresInSeconds`: Access Token 만료 시간
  - `refreshExpiresInSeconds`: Refresh Token 만료 시간 (추가 필요)

##### 3.2.3 JwtAuthenticationFilter
- **책임**: HTTP 요청 인터셉트 및 JWT 검증
- **상속**: `OncePerRequestFilter`
- **의존성**: `JwtProvider`, `CustomUserDetailsService`
- **동작 흐름**:
  1. Authorization 헤더에서 Bearer 토큰 추출
  2. JwtProvider로 토큰 검증
  3. 토큰에서 userId 추출
  4. CustomUserDetailsService로 UserDetails 로드
  5. Authentication 객체 생성 후 SecurityContext에 저장
- **예외 처리**:
  - 토큰이 없는 경우: 필터 통과 (인증 불필요한 엔드포인트)
  - 토큰이 유효하지 않은 경우: 401 Unauthorized

##### 3.2.4 CustomUserDetails
- **책임**: Spring Security의 UserDetails 인터페이스 구현
- **필드**:
  - `id`: 회원 ID
  - `email`: 이메일 (username으로 사용)
  - `password`: 암호화된 비밀번호
  - `authorities`: 권한 목록 (현재는 ROLE_USER 단일)
- **생성 방법**: Member 엔티티로부터 변환

##### 3.2.5 CustomUserDetailsService
- **책임**: UserDetailsService 구현
- **의존성**: `MemberRepository`
- **주요 메서드**:
  - `loadUserByUsername(String email)`: 이메일로 회원 조회 및 UserDetails 반환
  - 회원이 없으면 `UsernameNotFoundException` 발생

##### 3.2.6 SecurityConfig
- **책임**: Spring Security 전역 설정
- **의존성**:
  - `JwtAuthenticationFilter`
  - `JwtAuthenticationEntryPoint`
  - `PasswordEncoder`
- **설정 내용**:
  - HTTP 보안: CSRF 비활성화, Stateless 세션 관리
  - 인증 제외 경로: `/auth/**`, `/members` (회원가입), `/swagger-ui/**`, `/api-docs/**`
  - 인증 필요 경로: `/members/**` (조회/삭제), `/articles/**`
  - JWT 필터 추가: `UsernamePasswordAuthenticationFilter` 앞에 등록
  - 예외 핸들러: 인증 실패 시 커스텀 EntryPoint 사용

##### 3.2.7 JwtAuthenticationEntryPoint
- **책임**: 인증 실패 시 일관된 에러 응답 제공
- **구현**: `AuthenticationEntryPoint` 인터페이스
- **응답 형식**: 기존 `CommonApiResponse` 에러 형식과 일치
- **응답 코드**: `LOGIN_FAIL (A401, 401)`

##### 3.2.8 PasswordEncoderConfig
- **책임**: BCryptPasswordEncoder 빈 등록
- **어노테이션**: `@Configuration`
- **빈**: `PasswordEncoder` (BCrypt 알고리즘)

##### 3.2.9 RefreshTokenCleanupScheduler
- **책임**: 만료된 Refresh Token 주기적 삭제
- **어노테이션**: `@Scheduled(cron = "0 0 3 * * ?")` (매일 새벽 3시)
- **의존성**: `RefreshTokenRepository`
- **동작**: `deleteByExpiryDateBefore(LocalDateTime.now())` 호출

### 4. 클래스 간 의존성 관계

#### 4.1 요청 처리 흐름

```
[Client Request]
      ↓
[JwtAuthenticationFilter] ← JwtProvider
      ↓                      ↓
[SecurityContext 설정]   [토큰 검증]
      ↓
[AuthController] → [AuthService] → [MemberRepository]
                         ↓              ↓
                   [RefreshTokenRepository]
                         ↓
                   [JwtProvider]
                         ↓
      [CommonApiResponse 반환]
```

#### 4.2 의존성 그래프

```
AuthController
  └─→ AuthService
        ├─→ MemberRepository
        ├─→ RefreshTokenRepository
        ├─→ JwtProvider
        │     └─→ JwtProperties
        └─→ PasswordEncoder

JwtAuthenticationFilter
  ├─→ JwtProvider
  └─→ CustomUserDetailsService
        └─→ MemberRepository

SecurityConfig
  ├─→ JwtAuthenticationFilter
  ├─→ JwtAuthenticationEntryPoint
  └─→ PasswordEncoder

RefreshTokenCleanupScheduler
  └─→ RefreshTokenRepository
```

### 5. 데이터베이스 스키마 설계

#### 5.1 refresh_tokens 테이블
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_token (token),
    INDEX idx_member_id (member_id),
    INDEX idx_expiry_date (expiry_date),

    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);
```

#### 5.2 member 테이블 변경사항
- 기존 `password` 필드: 평문 저장 → BCrypt 해시 저장으로 변경
- 회원가입 시 `PasswordEncoder.encode(rawPassword)` 적용

### 6. 보안 정책

#### 6.1 비밀번호 정책
- **암호화**: BCrypt (강도 10)
- **저장**: 원본 비밀번호는 저장하지 않음, 해시만 저장
- **검증**: `PasswordEncoder.matches(raw, encoded)` 사용

#### 6.2 토큰 보안
- **Access Token**:
  - 짧은 유효기간 (1시간)
  - 서버에 저장하지 않음 (Stateless)
- **Refresh Token**:
  - 긴 유효기간 (14일)
  - DB에 저장하여 무효화 가능
  - RTR 적용으로 재사용 공격 방지
  - 블랙리스트로 로그아웃 시 즉시 무효화

#### 6.3 Refresh Token Rotation (RTR)
- **목적**: Refresh Token 탈취 시 피해 최소화
- **동작**:
  1. 클라이언트가 Refresh Token으로 갱신 요청
  2. 서버가 기존 토큰 검증 및 블랙리스트 처리
  3. 새로운 Access Token + Refresh Token 발급
  4. 클라이언트는 새 토큰으로 교체
- **효과**: 한 번 사용된 Refresh Token은 재사용 불가

#### 6.4 블랙리스트 정책
- **적용 시점**: 로그아웃, RTR 시 기존 토큰
- **저장 방식**: `is_blacklisted = true` 플래그 설정
- **검증**: 토큰 검증 시 블랙리스트 여부 확인
- **정리**: 만료 시간 경과 시 스케줄러로 자동 삭제

#### 6.5 자동 정리 정책
- **실행 주기**: 매일 새벽 3시
- **삭제 대상**: `expiry_date < 현재 시간`인 레코드
- **목적**: DB 용량 관리, 성능 최적화

### 7. API 명세

#### 7.1 로그인
```
POST /auth/login

Request Body:
{
  "email": "user@example.com",
  "password": "rawPassword123"
}

Response (200 OK):
{
  "code": "A201",
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}

Error Responses:
- 401: LOGIN_FAIL (이메일 또는 비밀번호 불일치)
- 404: MEMBER_NOT_FOUND
```

#### 7.2 토큰 갱신
```
POST /auth/refresh

Request Body:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

Response (200 OK):
{
  "code": "A202",
  "message": "토큰 갱신 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}

Error Responses:
- 401: TOKEN_EXPIRED (만료된 토큰)
- 401: TOKEN_INVALID (유효하지 않은 토큰)
- 401: TOKEN_BLACKLISTED (블랙리스트 토큰)
```

#### 7.3 로그아웃
```
POST /auth/logout

Request Body:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

Response (200 OK):
{
  "code": "A203",
  "message": "로그아웃 성공",
  "data": null
}
```

#### 7.4 인증이 필요한 API 예시
```
GET /members/{id}

Headers:
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

Response (200 OK): 기존과 동일

Error Responses:
- 401: TOKEN_MISSING (토큰 없음)
- 401: TOKEN_INVALID (유효하지 않은 토큰)
- 401: TOKEN_EXPIRED (만료된 토큰)
```

### 8. 에러 코드 추가

기존 `ErrorCode` enum에 추가 필요:

```
인증 관련:
- TOKEN_MISSING (A402, 401) - "토큰이 존재하지 않습니다"
- TOKEN_INVALID (A403, 401) - "유효하지 않은 토큰입니다"
- TOKEN_EXPIRED (A404, 401) - "만료된 토큰입니다"
- TOKEN_BLACKLISTED (A405, 401) - "블랙리스트 처리된 토큰입니다"
- REFRESH_TOKEN_NOT_FOUND (A406, 404) - "Refresh Token을 찾을 수 없습니다"
```

기존 `SuccessCode` enum에 추가 필요:

```
인증 관련:
- LOGIN_SUCCESS (A201) - "로그인 성공"
- TOKEN_REFRESH_SUCCESS (A202) - "토큰 갱신 성공"
- LOGOUT_SUCCESS (A203) - "로그아웃 성공"
```

### 9. application.yml 설정 추가

```yaml
security:
  jwt:
    secret: serverz-fighting-umm-umm....-yes-yes!!!!!
    expires-in-seconds: 3600                # Access Token: 1시간
    refresh-expires-in-seconds: 1209600     # Refresh Token: 14일
```

### 10. 구현 순서 권장사항

1. **Phase 1: 기본 인프라**
   - PasswordEncoderConfig 생성
   - JwtProperties 생성
   - JwtProvider 구현
   - 에러 코드 추가

2. **Phase 2: 도메인 계층**
   - RefreshToken Entity 생성
   - RefreshTokenRepository 생성
   - AuthService 구현
   - Auth DTO 생성

3. **Phase 3: Security 설정**
   - CustomUserDetails 구현
   - CustomUserDetailsService 구현
   - JwtAuthenticationFilter 구현
   - JwtAuthenticationEntryPoint 구현
   - SecurityConfig 구성

4. **Phase 4: API 계층**
   - AuthController 구현
   - 기존 MemberService에 PasswordEncoder 적용

5. **Phase 5: 유틸리티**
   - RefreshTokenCleanupScheduler 구현

6. **Phase 6: 테스트 및 검증**
   - 로그인 플로우 테스트
   - 토큰 갱신 테스트
   - 로그아웃 테스트
   - 인증이 필요한 API 테스트

### 11. 고려사항 및 제약사항

#### 11.1 성능 최적화
- Refresh Token 조회 시 인덱스 활용 (token, member_id, expiry_date)
- 블랙리스트 확인 시 쿼리 최적화 (`isBlacklisted = false` 조건)
- 만료된 토큰은 주기적으로 삭제하여 테이블 크기 관리

#### 11.2 확장 가능성
- 현재는 단일 권한 (ROLE_USER)만 지원
- 향후 역할 기반 접근 제어 (RBAC) 추가 가능
- Redis 등 인메모리 DB로 Refresh Token 저장소 변경 가능

#### 11.3 보안 강화 방안 (선택사항)
- IP 주소 기반 토큰 검증
- 디바이스 핑거프린팅
- 다중 디바이스 로그인 관리
- 비밀번호 변경 시 모든 토큰 무효화

#### 11.4 제약사항
- JWT는 Stateless이므로 Access Token은 만료 전 무효화 불가
- Refresh Token만 서버에서 관리 가능
- 토큰 크기가 커지면 네트워크 오버헤드 증가

### 12. 마이그레이션 계획

#### 12.1 기존 데이터 처리
- 기존 Member 테이블의 password 필드는 평문으로 저장되어 있을 가능성
- **옵션 1**: 전체 회원 비밀번호 초기화 요청 (권장하지 않음)
- **옵션 2**: 로그인 시점에 평문 비밀번호를 해시로 변환 (권장)
- **옵션 3**: 마이그레이션 스크립트로 일괄 변환 (불가능 - 평문 비밀번호 없음)

#### 12.2 하위 호환성
- 기존 API 엔드포인트는 유지
- 인증이 필요한 엔드포인트만 Authorization 헤더 추가
- 회원가입 API는 변경 없음 (내부적으로 암호화만 추가)

---

## 구현 완료 후 체크리스트

- [ ] 로그인 성공 시 Access Token + Refresh Token 발급
- [ ] Access Token으로 보호된 API 접근 가능
- [ ] Refresh Token으로 Access Token 갱신 가능
- [ ] RTR 적용으로 Refresh Token도 함께 갱신
- [ ] 로그아웃 시 Refresh Token 블랙리스트 처리
- [ ] 블랙리스트된 토큰으로 갱신 시도 시 실패
- [ ] 만료된 토큰 자동 삭제 (스케줄러)
- [ ] 회원가입 시 비밀번호 암호화 적용
- [ ] 로그인 시 비밀번호 검증 정상 동작
- [ ] 인증 실패 시 일관된 에러 응답
- [ ] Swagger UI에서 Authorization 헤더 입력 가능
- [ ] 모든 인증 관련 예외 처리 완료
