# Postman 테스트 시나리오

## 🚀 시작 전 준비

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 서버 확인
- URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 📋 테스트 시나리오 순서

### Phase 1: Member (회원) API 테스트
### Phase 2: Article (게시글) API 테스트
### Phase 3: 검색 기능 테스트
### Phase 4: N+1 문제 해결 검증
### Phase 5: 인덱스 성능 테스트

---

## Phase 1: Member API 테스트

### 1-1. 회원 생성

**Request**:
```http
POST http://localhost:8080/members
Content-Type: application/json

{
  "name": "김솝트",
  "birthDate": "2000-01-01",
  "email": "sopt1@sopt.org",
  "gender": "MALE"
}
```

**Expected Response** (201):
```json
{
  "code": "M202",
  "message": "회원가입 성공",
  "data": {
    "id": 1,
    "name": "김솝트",
    "birthDate": "2000-01-01",
    "email": "sopt1@sopt.org",
    "gender": "MALE"
  }
}
```

**테스트 포인트**:
- ✅ 상태 코드 200
- ✅ code가 "M202"
- ✅ id가 생성되었는지 확인
- ✅ 입력한 데이터가 그대로 반환되는지 확인

---

### 1-2. 회원 여러 명 생성 (게시글 테스트용)

**Request 1**:
```http
POST http://localhost:8080/members
Content-Type: application/json

{
  "name": "이솝트",
  "birthDate": "1998-05-15",
  "email": "sopt2@sopt.org",
  "gender": "FEMALE"
}
```

**Request 2**:
```http
POST http://localhost:8080/members
Content-Type: application/json

{
  "name": "박솝트",
  "birthDate": "1999-12-25",
  "email": "sopt3@sopt.org",
  "gender": "OTHER"
}
```

**Expected**: 각각 id 2, 3 생성

---

### 1-3. 중복 이메일 검증

**Request**:
```http
POST http://localhost:8080/members
Content-Type: application/json

{
  "name": "중복테스트",
  "birthDate": "2000-01-01",
  "email": "sopt1@sopt.org",
  "gender": "MALE"
}
```

**Expected Response** (400):
```json
{
  "code": "M402",
  "message": "이미 가입된 이메일입니다",
  "data": null
}
```

**테스트 포인트**:
- ✅ 상태 코드 400
- ✅ 에러 코드 "M402"

---

### 1-4. 나이 검증 (20세 미만)

**Request**:
```http
POST http://localhost:8080/members
Content-Type: application/json

{
  "name": "미성년자",
  "birthDate": "2010-01-01",
  "email": "minor@sopt.org",
  "gender": "MALE"
}
```

**Expected Response** (400):
```json
{
  "code": "M406",
  "message": "20세 미만은 회원 가입이 불가능합니다",
  "data": null
}
```

---

### 1-5. 회원 조회

**Request**:
```http
GET http://localhost:8080/members/1
```

**Expected Response** (200):
```json
{
  "code": "M204",
  "message": "회원 정보 조회 성공",
  "data": {
    "id": 1,
    "name": "김솝트",
    "birthDate": "2000-01-01",
    "email": "sopt1@sopt.org",
    "gender": "MALE"
  }
}
```

---

### 1-6. 존재하지 않는 회원 조회

**Request**:
```http
GET http://localhost:8080/members/999
```

**Expected Response** (404):
```json
{
  "code": "M401",
  "message": "회원을 찾을 수 없습니다",
  "data": null
}
```

---

### 1-7. 전체 회원 조회 (페이징)

**Request**:
```http
GET http://localhost:8080/members?page=0&size=10
```

**Expected Response** (200):
```json
{
  "code": "M204",
  "message": "회원 정보 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "김솝트",
        "birthDate": "2000-01-01",
        "email": "sopt1@sopt.org",
        "gender": "MALE"
      },
      {
        "id": 2,
        "name": "이솝트",
        "birthDate": "1998-05-15",
        "email": "sopt2@sopt.org",
        "gender": "FEMALE"
      },
      {
        "id": 3,
        "name": "박솝트",
        "birthDate": "1999-12-25",
        "email": "sopt3@sopt.org",
        "gender": "OTHER"
      }
    ],
    "pageable": {...},
    "totalElements": 3,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

**테스트 포인트**:
- ✅ totalElements가 3인지 확인
- ✅ content 배열에 3개 회원이 있는지 확인

---

## Phase 2: Article API 테스트

### 2-1. 게시글 생성 (첫 번째)

**Request**:
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 1,
  "title": "Spring Boot 시작하기",
  "content": "Spring Boot는 스프링 기반 애플리케이션을 빠르게 개발할 수 있게 해주는 프레임워크입니다.",
  "tag": "SPRING"
}
```

**Expected Response** (200):
```json
{
  "code": "A201",
  "message": "게시글 작성 성공",
  "data": {
    "id": 1,
    "authorId": 1,
    "authorName": "김솝트",
    "title": "Spring Boot 시작하기",
    "content": "Spring Boot는 스프링 기반 애플리케이션을 빠르게 개발할 수 있게 해주는 프레임워크입니다.",
    "tag": "SPRING",
    "createdAt": "2025-11-03T...",
    "updatedAt": "2025-11-03T..."
  }
}
```

**테스트 포인트**:
- ✅ 상태 코드 200
- ✅ code가 "A201"
- ✅ authorName이 "김솝트"로 자동 조회되는지 확인
- ✅ id가 생성되었는지 확인

---

### 2-2. 게시글 여러 개 생성

**Request 1** (authorId: 1, tag: DB):
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 1,
  "title": "MySQL 인덱스 최적화",
  "content": "인덱스를 적절히 사용하면 쿼리 성능을 100배 이상 향상시킬 수 있습니다.",
  "tag": "DB"
}
```

**Request 2** (authorId: 2, tag: SPRING):
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 2,
  "title": "JPA N+1 문제 해결",
  "content": "Fetch Join을 사용하면 N+1 쿼리 문제를 해결할 수 있습니다.",
  "tag": "SPRING"
}
```

**Request 3** (authorId: 2, tag: CS):
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 2,
  "title": "자료구조 Tree 정리",
  "content": "Tree는 계층적 구조를 표현하는 비선형 자료구조입니다.",
  "tag": "CS"
}
```

**Request 4** (authorId: 3, tag: DB):
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 3,
  "title": "Spring Data JPA vs QueryDSL",
  "content": "각 기술의 장단점과 사용 시점을 비교합니다.",
  "tag": "SPRING"
}
```

**Expected**: 각각 id 2, 3, 4, 5 생성

---

### 2-3. 존재하지 않는 회원으로 게시글 작성

**Request**:
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 999,
  "title": "존재하지 않는 작성자",
  "content": "이 게시글은 작성되지 않아야 합니다.",
  "tag": "ETC"
}
```

**Expected Response** (404):
```json
{
  "code": "M401",
  "message": "회원을 찾을 수 없습니다",
  "data": null
}
```

**테스트 포인트**:
- ✅ 상태 코드 404
- ✅ 작성자가 없으면 게시글 생성 불가

---

### 2-4. 중복 제목 검증

**Request**:
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 1,
  "title": "Spring Boot 시작하기",
  "content": "중복된 제목입니다.",
  "tag": "ETC"
}
```

**Expected Response** (400):
```json
{
  "code": "A403",
  "message": "이미 존재하는 게시글 제목입니다",
  "data": null
}
```

**테스트 포인트**:
- ✅ 상태 코드 400
- ✅ title 인덱스가 제대로 작동하는지 확인

---

### 2-5. 게시글 단건 조회

**Request**:
```http
GET http://localhost:8080/articles/1
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "id": 1,
    "authorId": 1,
    "authorName": "김솝트",
    "title": "Spring Boot 시작하기",
    "content": "Spring Boot는 스프링 기반 애플리케이션을 빠르게 개발할 수 있게 해주는 프레임워크입니다.",
    "tag": "SPRING",
    "createdAt": "2025-11-03T...",
    "updatedAt": "2025-11-03T..."
  }
}
```

---

### 2-6. 존재하지 않는 게시글 조회

**Request**:
```http
GET http://localhost:8080/articles/999
```

**Expected Response** (404):
```json
{
  "code": "A401",
  "message": "게시글을 찾을 수 없습니다",
  "data": null
}
```

---

### 2-7. 전체 게시글 조회 (페이징)

**Request**:
```http
GET http://localhost:8080/articles?page=0&size=10
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "authorId": 1,
        "authorName": "김솝트",
        "title": "Spring Boot 시작하기",
        "content": "...",
        "tag": "SPRING",
        "createdAt": "...",
        "updatedAt": "..."
      },
      // ... 나머지 게시글들
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

**테스트 포인트**:
- ✅ totalElements가 5인지 확인
- ✅ 모든 게시글에 authorName이 포함되어 있는지 확인

---

## Phase 3: 검색 기능 테스트

### 3-1. 제목으로 검색

**Request**:
```http
GET http://localhost:8080/articles?keyword=Spring
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 시작하기",
        "authorName": "김솝트",
        ...
      },
      {
        "id": 5,
        "title": "Spring Data JPA vs QueryDSL",
        "authorName": "박솝트",
        ...
      }
    ],
    "totalElements": 2
  }
}
```

**테스트 포인트**:
- ✅ "Spring"이 제목에 포함된 게시글만 반환
- ✅ totalElements가 2

---

### 3-2. 작성자 이름으로 검색

**Request**:
```http
GET http://localhost:8080/articles?keyword=이솝트
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [
      {
        "id": 3,
        "authorName": "이솝트",
        "title": "JPA N+1 문제 해결",
        ...
      },
      {
        "id": 4,
        "authorName": "이솝트",
        "title": "자료구조 Tree 정리",
        ...
      }
    ],
    "totalElements": 2
  }
}
```

**테스트 포인트**:
- ✅ authorName이 "이솝트"인 게시글만 반환
- ✅ totalElements가 2

---

### 3-3. 제목 또는 작성자 이름 검색

**Request**:
```http
GET http://localhost:8080/articles?keyword=인덱스
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [
      {
        "id": 2,
        "title": "MySQL 인덱스 최적화",
        "authorName": "김솝트",
        ...
      }
    ],
    "totalElements": 1
  }
}
```

---

### 3-4. 검색 결과 없음

**Request**:
```http
GET http://localhost:8080/articles?keyword=존재하지않는키워드
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [],
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**테스트 포인트**:
- ✅ 빈 배열 반환
- ✅ 에러가 아닌 정상 응답 (200)

---

### 3-5. 키워드 없이 조회 (전체 조회)

**Request**:
```http
GET http://localhost:8080/articles
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [...],
    "totalElements": 5
  }
}
```

**테스트 포인트**:
- ✅ 키워드가 없으면 전체 게시글 반환
- ✅ totalElements가 5

---

### 3-6. 페이징과 검색 조합

**Request**:
```http
GET http://localhost:8080/articles?keyword=Spring&page=0&size=1
```

**Expected Response** (200):
```json
{
  "code": "A202",
  "message": "게시글 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 시작하기",
        ...
      }
    ],
    "totalElements": 2,
    "totalPages": 2,
    "size": 1,
    "number": 0
  }
}
```

**테스트 포인트**:
- ✅ size=1이므로 1개만 반환
- ✅ totalElements는 2 (전체 검색 결과)
- ✅ totalPages는 2 (2개를 1개씩 나누면 2페이지)

---

## Phase 4: N+1 문제 해결 검증

### 4-1. 콘솔 로그 확인

**애플리케이션 재시작 후 로그 레벨 확인**:
`application.yml`에서 이미 설정되어 있음:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
```

**Request**:
```http
GET http://localhost:8080/articles?page=0&size=5
```

**콘솔에서 확인할 사항**:

✅ **올바른 경우** (Fetch Join 적용):
```sql
Hibernate:
    select
        a1_0.id,
        a1_0.author_id,
        m1_0.id,
        m1_0.name,
        m1_0.email,
        ...
    from
        article a1_0
    join
        member m1_0
            on m1_0.id=a1_0.author_id
    limit
        ?
```
- **쿼리 1개**: article과 member를 한 번에 JOIN으로 조회
- **"join member"** 구문이 있어야 함

❌ **잘못된 경우** (N+1 발생):
```sql
Hibernate: select ... from article ... limit ?
Hibernate: select ... from member where id=?
Hibernate: select ... from member where id=?
Hibernate: select ... from member where id=?
Hibernate: select ... from member where id=?
Hibernate: select ... from member where id=?
```
- 쿼리 6개 (1 + 5): article 1번 + member 5번
- 각 article마다 member를 개별 조회

---

### 4-2. 검색 쿼리 N+1 검증

**Request**:
```http
GET http://localhost:8080/articles?keyword=Spring
```

**콘솔에서 확인**:
```sql
Hibernate:
    select
        a1_0.id,
        a1_0.author_id,
        m1_0.id,        -- member도 함께 SELECT
        m1_0.name,
        ...
    from
        article a1_0
    join
        member m1_0     -- Fetch Join
            on m1_0.id=a1_0.author_id
    where
        a1_0.title like ? escape '!'
        or m1_0.name like ? escape '!'
```

**테스트 포인트**:
- ✅ 쿼리가 1개만 실행되는지 확인
- ✅ JOIN 절이 포함되어 있는지 확인
- ✅ SELECT 절에 member 컬럼들이 포함되어 있는지 확인

---

## Phase 5: 인덱스 성능 테스트

### 5-1. 중복 제목 체크 성능 (title 인덱스)

**대량 데이터 생성 후 테스트** (옵션):

만약 성능을 체감하고 싶다면:
1. 게시글 10,000개 이상 생성
2. 중복 제목 체크 시간 측정

**Request**:
```http
POST http://localhost:8080/articles
Content-Type: application/json

{
  "authorId": 1,
  "title": "성능 테스트 - 고유 제목 12345",
  "content": "인덱스 성능 테스트",
  "tag": "ETC"
}
```

**DB 쿼리 확인** (MySQL 접속):
```sql
EXPLAIN SELECT * FROM article WHERE title = 'Spring Boot 시작하기';
```

**Expected**:
```
+----+-------+--------------------+-------+
| id | type  | key                | rows  |
+----+-------+--------------------+-------+
|  1 | ref   | idx_article_title  |     1 |
+----+-------+--------------------+-------+
```

**테스트 포인트**:
- ✅ `key` 컬럼에 `idx_article_title`이 표시되는지 확인
- ✅ `type`이 `ref` 또는 `const`인지 확인 (인덱스 사용)
- ❌ `type`이 `ALL`이면 Full Table Scan (인덱스 미사용)

---

## 🎯 전체 테스트 체크리스트

### Member API
- [ ] 1-1. 회원 생성 성공
- [ ] 1-2. 회원 3명 생성 (테스트 데이터)
- [ ] 1-3. 중복 이메일 검증 (400 에러)
- [ ] 1-4. 나이 검증 (400 에러)
- [ ] 1-5. 회원 조회 성공
- [ ] 1-6. 존재하지 않는 회원 조회 (404 에러)
- [ ] 1-7. 전체 회원 조회 (페이징)

### Article API
- [ ] 2-1. 게시글 생성 성공
- [ ] 2-2. 게시글 5개 생성 (테스트 데이터)
- [ ] 2-3. 존재하지 않는 회원으로 게시글 작성 (404 에러)
- [ ] 2-4. 중복 제목 검증 (400 에러)
- [ ] 2-5. 게시글 단건 조회
- [ ] 2-6. 존재하지 않는 게시글 조회 (404 에러)
- [ ] 2-7. 전체 게시글 조회 (페이징)

### 검색 기능
- [ ] 3-1. 제목으로 검색 (keyword=Spring)
- [ ] 3-2. 작성자 이름으로 검색 (keyword=이솝트)
- [ ] 3-3. 제목/작성자 검색 (keyword=인덱스)
- [ ] 3-4. 검색 결과 없음 (빈 배열)
- [ ] 3-5. 키워드 없이 조회 (전체 조회)
- [ ] 3-6. 페이징과 검색 조합

### N+1 문제 해결 검증
- [ ] 4-1. 전체 조회 시 콘솔에서 쿼리 1개만 실행 확인
- [ ] 4-2. 검색 시 콘솔에서 JOIN 쿼리 확인

### 인덱스 검증
- [ ] 5-1. EXPLAIN으로 idx_article_title 인덱스 사용 확인

---

## 🐛 예상 문제 해결

### 문제 1: "Member를 찾을 수 없습니다" (404)
**원인**: Member를 먼저 생성하지 않았음
**해결**: Phase 1을 먼저 실행하여 Member 생성

### 문제 2: "이미 존재하는 게시글 제목입니다" (400)
**원인**: 같은 제목의 게시글이 이미 존재
**해결**: title을 다른 값으로 변경

### 문제 3: N+1 쿼리가 여전히 발생
**원인**: Fetch Join이 제대로 적용되지 않음
**해결**:
1. ArticleRepository.java의 @Query 확인
2. `JOIN FETCH a.author` 구문이 있는지 확인

### 문제 4: 인덱스가 사용되지 않음
**원인**: DB에 인덱스가 생성되지 않음
**해결**:
1. 애플리케이션 재시작 (Hibernate가 인덱스 생성)
2. MySQL에서 직접 확인: `SHOW INDEX FROM article;`

---

## 📊 성능 비교 (선택 사항)

### Before (Fetch Join 적용 전)
```
GET /articles (20개)
→ SQL 쿼리: 21개 (1 + 20)
→ 응답 시간: ~100ms
```

### After (Fetch Join 적용 후)
```
GET /articles (20개)
→ SQL 쿼리: 1개
→ 응답 시간: ~10ms
```

**성능 향상**: **10배** ⚡

---

## 💡 추가 테스트 아이디어

### 1. Stress Test (대량 데이터)
```http
# 스크립트로 게시글 1000개 생성 후
GET http://localhost:8080/articles?page=0&size=100
```

### 2. Edge Case
```http
# 특수문자 검색
GET http://localhost:8080/articles?keyword=%20

# 빈 문자열
GET http://localhost:8080/articles?keyword=

# 매우 긴 키워드
GET http://localhost:8080/articles?keyword=매우긴키워드...
```

### 3. 동시 요청 테스트
Postman Collection Runner로 동시에 여러 요청 실행

---

**작성일**: 2025-11-03
**테스트 대상**: SOPT Assignment Article API
**예상 소요 시간**: 20-30분
