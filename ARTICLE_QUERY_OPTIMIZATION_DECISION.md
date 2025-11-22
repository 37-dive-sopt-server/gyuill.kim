# JPA만으로는 부족할까? JOOQ vs QueryDSL vs Native Query 현실적인 선택 가이드

> "좋은 기술은 많지만, 지금 우리 프로젝트에 맞는 기술은 따로 있다"

## 들어가며

Spring Boot로 개발하다 보면 한 번쯤 고민하게 됩니다.

```java
@Query("SELECT a FROM Article a JOIN FETCH a.author WHERE a.title LIKE %:keyword%")
Page<Article> search(String keyword, Pageable pageable);
```

"이 JPQL 문자열... 타입 안전하지 않은데, QueryDSL 도입해야 하나?"
"복잡한 쿼리는 JOOQ가 낫다던데?"
"그냥 Native Query 쓰면 안 되나?"

저도 최근 소규모 프로젝트를 진행하면서 이런 고민을 했고, 결론적으로 **아무것도 도입하지 않기로** 결정했습니다.

이 글에서는 그 이유와, 각 기술을 언제 선택해야 하는지 실무 관점에서 정리해보겠습니다.

---

## 목차
1. [문제 인식: JPA만으로 부족한 순간](#문제-인식-jpa만으로-부족한-순간)
2. [세 가지 선택지 비교](#세-가지-선택지-비교)
3. [JOOQ: SQL을 Java로](#jooq-sql을-java로)
4. [QueryDSL: JPA의 든든한 파트너](#querydsl-jpa의-든든한-파트너)
5. [Native Query: 가장 직접적인 방법](#native-query-가장-직접적인-방법)
6. [의사결정 프레임워크](#의사결정-프레임워크)
7. [실전 사례: 내 프로젝트 분석](#실전-사례-내-프로젝트-분석)
8. [결론: 과하지 않게, 부족하지 않게](#결론-과하지-않게-부족하지-않게)

---

## 문제 인식: JPA만으로 부족한 순간

### 시나리오 1: 동적 쿼리의 늪

```java
// 요구사항: 검색 기능
// - keyword (선택)
// - tag (선택)
// - 작성자 (선택)
// - 날짜 범위 (선택)

// JPQL로 하면?
@Query("SELECT a FROM Article a JOIN FETCH a.author WHERE " +
       "(:keyword IS NULL OR a.title LIKE %:keyword%) AND " +
       "(:tag IS NULL OR a.tag = :tag) AND " +
       "(:authorName IS NULL OR a.author.name = :authorName) AND " +
       "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
       "(:endDate IS NULL OR a.createdAt <= :endDate)")
Page<Article> search(@Param("keyword") String keyword,
                     @Param("tag") Tag tag,
                     @Param("authorName") String authorName,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     Pageable pageable);
```

**문제점**:
- 😵 쿼리 문자열이 너무 길고 복잡
- 😵 파라미터가 많아질수록 가독성 저하
- 😵 조건 추가/제거 시 오타 발생 위험

### 시나리오 2: 타입 안정성 부족

```java
// 엔티티 필드명 변경
public class Article {
    private String title;  // → subtitle로 변경
}

// JPQL은 런타임에 에러 발생 😱
@Query("SELECT a FROM Article a WHERE a.title = :title")  // 컴파일은 성공, 실행 시 에러

// QueryDSL은 컴파일 에러 발생 ✅
article.title.eq(title)  // IDE가 즉시 "title 없음" 경고
```

### 시나리오 3: 복잡한 집계 쿼리

```java
// 요구사항: 월별 작성자별 게시글 수 통계
SELECT
    YEAR(a.created_at) as year,
    MONTH(a.created_at) as month,
    m.name as author,
    COUNT(*) as count,
    RANK() OVER (PARTITION BY YEAR(a.created_at), MONTH(a.created_at) ORDER BY COUNT(*) DESC) as rank
FROM article a
JOIN member m ON a.author_id = m.id
GROUP BY YEAR(a.created_at), MONTH(a.created_at), m.name
```

**문제점**:
- 😵 JPQL로 윈도우 함수 표현 어려움
- 😵 Native Query 쓰자니 타입 안정성 포기
- 😵 결과를 DTO로 매핑하는 코드 지저분

이런 순간, 우리는 대안을 찾게 됩니다.

---

## 세 가지 선택지 비교

### 한눈에 보는 비교표

| 특성 | JPA + JPQL | QueryDSL | JOOQ | Native Query |
|------|-----------|----------|------|--------------|
| **타입 안정성** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |
| **동적 쿼리** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |
| **JPA 통합** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **SQL 제어** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **학습 곡선** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **초기 설정** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **복잡한 쿼리** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

### 아키텍처 철학 비교

```
JPA/JPQL:     객체 지향 → 데이터베이스
              "객체를 다루듯이 쿼리를 작성하자"

QueryDSL:     객체 지향 + 타입 안정성
              "JPA의 철학을 유지하되, 더 안전하게"

JOOQ:         데이터베이스 → 객체 지향
              "SQL이 진리다. Java로 표현하자"

Native Query: SQL 그 자체
              "가장 직접적이고 명확하게"
```

---

## JOOQ: SQL을 Java로

### 핵심 개념

JOOQ는 **"Database First"** 철학을 가집니다.

> "SQL을 잘 아는 개발자라면, 그 지식을 그대로 활용하자"

#### 1. 데이터베이스 스키마에서 코드 생성

```sql
-- 데이터베이스 스키마
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL
);
```

```bash
# Gradle 플러그인으로 Java 클래스 자동 생성
./gradlew jooqGenerate
```

```java
// 생성된 코드 (자동)
public class Article extends TableImpl<ArticleRecord> {
    public static final Article ARTICLE = new Article();

    public final TableField<ArticleRecord, Long> ID = createField("id", ...);
    public final TableField<ArticleRecord, String> TITLE = createField("title", ...);
    public final TableField<ArticleRecord, Long> AUTHOR_ID = createField("author_id", ...);
    // ...
}
```

#### 2. SQL과 1:1 매핑되는 Java API

```sql
-- SQL
SELECT a.title, m.name
FROM article a
JOIN member m ON a.author_id = m.id
WHERE a.title LIKE '%Spring%'
  AND a.created_at >= '2024-01-01'
ORDER BY a.created_at DESC
LIMIT 10;
```

```java
// JOOQ (거의 동일한 구조)
dslContext
    .select(ARTICLE.TITLE, MEMBER.NAME)
    .from(ARTICLE)
    .join(MEMBER).on(ARTICLE.AUTHOR_ID.eq(MEMBER.ID))
    .where(ARTICLE.TITLE.like("%Spring%")
        .and(ARTICLE.CREATED_AT.greaterOrEqual(LocalDateTime.of(2024, 1, 1, 0, 0))))
    .orderBy(ARTICLE.CREATED_AT.desc())
    .limit(10)
    .fetch();
```

**특징**:
- SQL을 알면 JOOQ도 바로 사용 가능
- `ARTICLE.TITLE`, `MEMBER.NAME`은 컴파일 타임 검증
- 데이터베이스 스키마 변경 시 빌드 에러 발생 (안전)

#### 3. 강력한 타입 안정성

```java
// 실수로 Long 타입 필드에 String 대입 시도
dslContext
    .update(ARTICLE)
    .set(ARTICLE.ID, "문자열")  // ❌ 컴파일 에러!
    .execute();

// 존재하지 않는 필드 접근
dslContext
    .select(ARTICLE.NICKNAME)  // ❌ 컴파일 에러! (nickname 필드 없음)
    .from(ARTICLE)
    .fetch();
```

### JOOQ의 강점

#### 1. 복잡한 SQL을 완벽하게 표현

```java
// 윈도우 함수
dslContext
    .select(
        ARTICLE.ID,
        ARTICLE.TITLE,
        rowNumber().over()
            .partitionBy(ARTICLE.AUTHOR_ID)
            .orderBy(ARTICLE.CREATED_AT.desc())
            .as("row_num")
    )
    .from(ARTICLE)
    .fetch();

// CTE (Common Table Expression)
dslContext
    .with("recent_articles").as(
        select(ARTICLE.ID, ARTICLE.TITLE)
            .from(ARTICLE)
            .where(ARTICLE.CREATED_AT.greaterThan(LocalDateTime.now().minusDays(7)))
    )
    .select()
    .from(table(name("recent_articles")))
    .fetch();

// MySQL 특화 기능
dslContext
    .insertInto(ARTICLE)
    .set(ARTICLE.TITLE, "제목")
    .onDuplicateKeyUpdate()
    .set(ARTICLE.TITLE, "수정된 제목")
    .execute();
```

#### 2. 데이터베이스 특화 최적화

```java
// Batch Insert (대량 삽입 최적화)
dslContext
    .batchInsert(articles)
    .execute();

// MySQL FULLTEXT SEARCH
dslContext
    .select()
    .from(ARTICLE)
    .where(DSL.condition("MATCH(title, content) AGAINST ({0} IN BOOLEAN MODE)", keyword))
    .fetch();
```

### JOOQ의 약점

#### 1. JPA와의 불편한 공존

```java
// JPA Entity
@Entity
public class Article {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;  // 객체 참조
}

// JOOQ Record
ArticleRecord record = dslContext
    .selectFrom(ARTICLE)
    .where(ARTICLE.ID.eq(1L))
    .fetchOne();

Long authorId = record.getAuthorId();  // ❌ FK만 있음, 객체 아님
// Member 객체를 가져오려면 별도로 조인 필요
```

**문제점**:
- JPA는 객체 그래프 탐색 (article.getAuthor().getName())
- JOOQ는 FK만 관리 (article.getAuthorId())
- 도메인 모델이 이중으로 관리됨

#### 2. 트랜잭션 동기화 복잡도

```java
@Transactional
public void updateArticle(Long articleId, String newTitle) {
    // JPA로 조회
    Article article = articleRepository.findById(articleId).get();

    // JPA 변경 감지 (Dirty Checking)
    article.setTitle(newTitle);  // UPDATE 쿼리 자동 생성

    // JOOQ로 조회수 증가
    dslContext
        .update(ARTICLE)
        .set(ARTICLE.VIEW_COUNT, ARTICLE.VIEW_COUNT.plus(1))
        .where(ARTICLE.ID.eq(articleId))
        .execute();

    // ⚠️ 문제: JPA 영속성 컨텍스트와 JOOQ 변경사항이 동기화 안될 수 있음
}
```

#### 3. 높은 초기 설정 비용

```gradle
// build.gradle
jooq {
    configurations {
        main {
            generationTool {
                jdbc {
                    url = 'jdbc:mysql://localhost:3306/mydb'
                    user = 'root'
                    password = 'password'  // ⚠️ 보안 이슈
                }
                database {
                    name = 'org.jooq.meta.mysql.MySQLDatabase'
                    inputSchema = 'mydb'
                }
                generate {
                    pojos = true
                    daos = true
                }
            }
        }
    }
}
```

**문제점**:
- 빌드 시 데이터베이스 연결 필요 (CI/CD 복잡도 증가)
- 개발자마다 로컬 DB 설정 필요
- 스키마 변경 시마다 재생성 필요

#### 4. ORM의 이점 포기

```java
// JPA: 1차 캐시, Lazy Loading, Dirty Checking 자동
Article article = repository.findById(1L).get();
article.setTitle("새 제목");  // 자동 UPDATE

// JOOQ: 모든 것을 명시적으로
ArticleRecord record = dslContext.selectFrom(ARTICLE).where(ARTICLE.ID.eq(1L)).fetchOne();
record.setTitle("새 제목");
record.update();  // 명시적 호출 필요
```

### JOOQ 도입 적합 시점

#### ✅ 이런 경우 고려하세요

1. **레거시 데이터베이스 통합**
   - 변경할 수 없는 복잡한 스키마
   - ORM 매핑이 불가능한 구조
   - 이미 최적화된 SQL이 많이 존재

2. **복잡한 분석/리포팅 쿼리**
   ```sql
   -- 이런 쿼리를 자주 작성한다면
   SELECT
       YEAR(created_at) as year,
       MONTH(created_at) as month,
       author_id,
       COUNT(*) as count,
       AVG(view_count) as avg_views,
       RANK() OVER (PARTITION BY YEAR(created_at) ORDER BY COUNT(*) DESC) as rank
   FROM article
   GROUP BY YEAR(created_at), MONTH(created_at), author_id
   HAVING COUNT(*) > 10
   ```

3. **성능이 매우 중요한 대용량 처리**
   - Batch 처리 최적화 필수
   - SQL 튜닝 완전 제어 필요
   - 매 쿼리마다 실행 계획 최적화

4. **Database-First 팀 철학**
   - DBA가 스키마 설계 주도
   - SQL 전문가가 많은 팀
   - 데이터베이스를 Single Source of Truth로

#### ❌ 이런 경우 비추천

- JPA 중심 프로젝트
- 소규모 CRUD 애플리케이션
- 엔티티 < 20개
- 동적 쿼리만 필요한 경우

---

## QueryDSL: JPA의 든든한 파트너

### 핵심 개념

QueryDSL은 **"Application First"** 철학을 가집니다.

> "JPA의 장점을 유지하되, 코드로 타입 안전하게 쿼리를 작성하자"

#### 1. JPA 엔티티 기반 코드 생성

```java
// JPA Entity
@Entity
public class Article {
    @Id @GeneratedValue
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;
}
```

```bash
# Gradle 빌드 시 자동 생성
./gradlew build
```

```java
// 생성된 Q클래스 (자동)
public class QArticle extends EntityPathBase<Article> {
    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final StringPath title = createString("title");
    public final QMember author = new QMember(forProperty("author"));
    // ...
}
```

#### 2. JPA와 완벽한 통합

```java
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Article> searchArticles(ArticleSearchCondition condition, Pageable pageable) {
        QArticle article = QArticle.article;
        QMember member = QMember.member;

        List<Article> content = queryFactory
            .selectFrom(article)
            .join(article.author, member).fetchJoin()  // ✅ JPA Fetch Join
            .where(
                titleContains(condition.getKeyword()),
                tagEq(condition.getTag())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();  // ✅ JPA Entity 반환

        return new PageImpl<>(content, pageable, countQuery(condition));
    }
}
```

**특징**:
- JPA 엔티티를 그대로 반환 → 영속성 컨텍스트 유지
- Fetch Join, Lazy Loading 모두 가능
- `@Transactional`과 완벽 호환

#### 3. 동적 쿼리의 우아함

```java
// BooleanExpression으로 조건 조합
private BooleanExpression titleContains(String keyword) {
    return keyword != null ? article.title.contains(keyword) : null;
}

private BooleanExpression authorNameContains(String authorName) {
    return authorName != null ? article.author.name.contains(authorName) : null;
}

private BooleanExpression tagEq(Tag tag) {
    return tag != null ? article.tag.eq(tag) : null;
}

private BooleanExpression createdDateBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null && end == null) return null;
    if (start == null) return article.createdAt.loe(end);
    if (end == null) return article.createdAt.goe(start);
    return article.createdAt.between(start, end);
}

// 사용
List<Article> results = queryFactory
    .selectFrom(article)
    .where(
        titleContains(condition.getKeyword()),  // null이면 자동으로 무시
        authorNameContains(condition.getAuthorName()),
        tagEq(condition.getTag()),
        createdDateBetween(condition.getStartDate(), condition.getEndDate())
    )
    .fetch();
```

**장점**:
- 조건 메서드 재사용 가능
- null 처리 자동
- 조건 조합 자유로움
- 가독성 우수

### QueryDSL의 강점

#### 1. 낮은 학습 곡선

```java
// JPA를 알면 QueryDSL도 쉽게 배움

// JPQL
@Query("SELECT a FROM Article a WHERE a.title = :title")
List<Article> findByTitle(@Param("title") String title);

// QueryDSL (거의 동일한 느낌)
List<Article> results = queryFactory
    .selectFrom(article)
    .where(article.title.eq(title))
    .fetch();
```

#### 2. IDE 지원 + 자동 리팩토링

```java
// 엔티티 필드명 변경: title → subject
private String title;  // → private String subject;

// QueryDSL 코드
article.title.eq("test")
// ↓ IDE 리팩토링 기능으로 자동 변경
article.subject.eq("test")  // ✅ 자동 수정

// JPQL 문자열
@Query("SELECT a FROM Article a WHERE a.title = :title")
// ↓ 자동 수정 안됨
@Query("SELECT a FROM Article a WHERE a.title = :title")  // ⚠️ 여전히 title
```

#### 3. Projection으로 성능 최적화

```java
// DTO 직접 조회 (필요한 컬럼만 SELECT)
public List<ArticleSimpleDto> findSimpleArticles() {
    QArticle article = QArticle.article;
    QMember member = QMember.member;

    return queryFactory
        .select(Projections.constructor(
            ArticleSimpleDto.class,
            article.id,
            article.title,
            member.name,
            article.createdAt
        ))
        .from(article)
        .join(article.author, member)
        .fetch();
}

// 생성되는 SQL
// SELECT a.id, a.title, m.name, a.created_at
// FROM article a
// JOIN member m ON a.author_id = m.id
```

**장점**:
- 엔티티 전체를 조회하지 않아 성능 향상
- DTO 생성자 직접 호출
- N+1 문제 원천 차단

#### 4. 복잡한 서브쿼리

```java
// 서브쿼리 예시: 평균보다 조회수가 높은 게시글
QArticle article = QArticle.article;
QArticle articleSub = new QArticle("articleSub");

List<Article> results = queryFactory
    .selectFrom(article)
    .where(article.viewCount.gt(
        JPAExpressions
            .select(articleSub.viewCount.avg())
            .from(articleSub)
    ))
    .fetch();
```

### QueryDSL의 약점

#### 1. JOOQ 대비 SQL 제어력 약함

```java
// QueryDSL로 어려운 것들

// 1. 윈도우 함수 (제한적)
// 2. 데이터베이스 특화 함수
// 3. 복잡한 CTE

// 해결책: Native Query 혼용
@Query(value = "SELECT * FROM article WHERE MATCH(title) AGAINST (?1 IN BOOLEAN MODE)",
       nativeQuery = true)
List<Article> fullTextSearch(String keyword);
```

#### 2. Annotation Processing 설정 필요

```gradle
// build.gradle
dependencies {
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
}

// Q클래스 생성 경로 설정
def querydslDir = "$buildDir/generated/querydsl"

sourceSets {
    main.java.srcDirs += [ querydslDir ]
}
```

**문제점**:
- 초기 설정이 약간 복잡 (하지만 JOOQ보다는 훨씬 간단)
- IDE 설정 필요 (IntelliJ는 대부분 자동)

#### 3. 쿼리 실행 시점의 불명확성 (미미)

```java
// JPQL: 선언 시점에 파싱 (어느 정도)
@Query("SELECT a FROM Article a WHERE a.title = :title")

// QueryDSL: 실행 시점에 쿼리 생성
queryFactory.selectFrom(article).where(article.title.eq(title)).fetch();
```

하지만 컴파일 타임 타입 체크로 대부분의 에러는 방지됨

### QueryDSL 도입 적합 시점

#### ✅ 이런 경우 강력 추천

1. **JPA 기반 프로젝트에서 동적 쿼리 필요**
   ```java
   // 검색 조건이 3개 이상 선택적으로 조합
   searchArticles(keyword, tag, authorName, startDate, endDate);
   ```

2. **타입 안정성 + 가독성 중시**
   ```java
   // JPQL 문자열보다 코드가 더 명확
   queryFactory
       .selectFrom(article)
       .where(article.status.eq(Status.PUBLISHED))
       .orderBy(article.createdAt.desc())
       .fetch();
   ```

3. **중규모 이상 프로젝트**
   - 엔티티 10개 이상
   - 복잡한 검색 기능 여러 개
   - 장기 유지보수 예정

4. **팀 협업 중시**
   - 코드 리뷰 시 가독성 중요
   - 리팩토링 빈번
   - 신입 개발자 온보딩 고려

#### ❌ 이런 경우 불필요

- 엔티티 < 5개
- 단순 CRUD만
- 동적 쿼리 거의 없음
- 1~2주 단기 프로젝트

---

## Native Query: 가장 직접적인 방법

### 핵심 개념

Native Query는 **"SQL을 직접 작성"**합니다.

> "복잡한 건 생각하지 말고, SQL 그대로 쓰자"

#### 1. 기본 사용법

```java
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Native Query
    @Query(value = "SELECT * FROM article WHERE title LIKE %:keyword%",
           nativeQuery = true)
    List<Article> searchByTitle(@Param("keyword") String keyword);

    // DTO 매핑
    @Query(value = "SELECT a.id, a.title, m.name as author_name " +
                   "FROM article a " +
                   "JOIN member m ON a.author_id = m.id " +
                   "WHERE a.created_at >= :date",
           nativeQuery = true)
    List<ArticleDto> findRecentArticles(@Param("date") LocalDateTime date);
}
```

#### 2. EntityManager 활용

```java
@Repository
@RequiredArgsConstructor
public class ArticleQueryRepository {
    private final EntityManager em;

    public List<Article> complexSearch(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM article WHERE 1=1");

        if (params.containsKey("keyword")) {
            sql.append(" AND title LIKE :keyword");
        }
        if (params.containsKey("tag")) {
            sql.append(" AND tag = :tag");
        }

        Query query = em.createNativeQuery(sql.toString(), Article.class);

        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
```

### Native Query의 강점

#### 1. 최고의 SQL 제어력

```java
// 데이터베이스 특화 기능 100% 활용
@Query(value = "SELECT * FROM article " +
               "WHERE MATCH(title, content) AGAINST (?1 IN BOOLEAN MODE)",
       nativeQuery = true)
List<Article> fullTextSearch(String keyword);

// 복잡한 윈도우 함수
@Query(value = "SELECT *, " +
               "ROW_NUMBER() OVER (PARTITION BY author_id ORDER BY created_at DESC) as rn " +
               "FROM article",
       nativeQuery = true)
List<Object[]> rankingByAuthor();
```

#### 2. 즉시 적용 가능

```java
// 설정 불필요, 바로 사용
@Query(value = "SELECT * FROM article LIMIT 10", nativeQuery = true)
List<Article> findTop10();
```

#### 3. 디버깅 용이

```java
// SQL을 그대로 복사해서 데이터베이스 클라이언트에서 실행 가능
String sql = "SELECT a.*, m.name " +
             "FROM article a " +
             "JOIN member m ON a.author_id = m.id " +
             "WHERE a.created_at > '2024-01-01'";

// 복사 → MySQL Workbench/DBeaver에 붙여넣기 → 즉시 실행
```

### Native Query의 약점

#### 1. 타입 안정성 전혀 없음

```java
// 오타가 있어도 컴파일 성공
@Query(value = "SELECT * FROM articel WHERE titl = :title",  // ⚠️ 오타!
       nativeQuery = true)
List<Article> search(@Param("title") String title);

// 런타임에 에러 발생
// org.hibernate.exception.SQLGrammarException: could not execute query
```

#### 2. 데이터베이스 종속성

```java
// MySQL 전용
@Query(value = "SELECT * FROM article LIMIT 10", nativeQuery = true)

// PostgreSQL로 변경 시 동작 안함 → LIMIT 10 → FETCH FIRST 10 ROWS ONLY
```

#### 3. 동적 쿼리 작성 복잡

```java
// String 조합 지옥
public List<Article> dynamicSearch(String keyword, Tag tag, LocalDateTime date) {
    StringBuilder sql = new StringBuilder("SELECT * FROM article WHERE 1=1");
    Map<String, Object> params = new HashMap<>();

    if (keyword != null) {
        sql.append(" AND title LIKE :keyword");
        params.put("keyword", "%" + keyword + "%");
    }

    if (tag != null) {
        sql.append(" AND tag = :tag");
        params.put("tag", tag.name());
    }

    if (date != null) {
        sql.append(" AND created_at >= :date");
        params.put("date", date);
    }

    Query query = em.createNativeQuery(sql.toString(), Article.class);
    params.forEach(query::setParameter);

    return query.getResultList();
}
```

**문제점**:
- 가독성 낮음
- 오타 위험
- SQL 인젝션 위험 (파라미터 바인딩 실수 시)

#### 4. DTO 매핑 불편

```java
// Native Query 결과를 DTO로 매핑
@Query(value = "SELECT a.id, a.title, m.name as author_name FROM ...",
       nativeQuery = true)
List<Object[]> findArticlesRaw();

// 수동 매핑 필요
public List<ArticleDto> findArticles() {
    return findArticlesRaw().stream()
        .map(row -> new ArticleDto(
            (Long) row[0],
            (String) row[1],
            (String) row[2]
        ))
        .toList();
}
```

### Native Query 도입 적합 시점

#### ✅ 이런 경우 사용하세요

1. **데이터베이스 특화 기능 필요**
   ```sql
   -- MySQL FULLTEXT SEARCH
   MATCH(title, content) AGAINST ('keyword' IN BOOLEAN MODE)

   -- PostgreSQL JSON 연산
   SELECT * FROM article WHERE metadata->'tags' @> '["spring"]'
   ```

2. **일회성 복잡한 쿼리**
   ```java
   // 리포트 생성용 복잡한 집계
   @Query(value = "SELECT ... 100줄짜리 SQL ...", nativeQuery = true)
   List<ReportDto> generateReport();
   ```

3. **레거시 SQL 마이그레이션**
   ```java
   // 기존에 작동하는 SQL을 그대로 사용
   @Query(value = "기존 SQL 복사 붙여넣기", nativeQuery = true)
   ```

4. **성능 최적화 극한**
   ```java
   // 힌트, 인덱스 강제 지정 등
   @Query(value = "SELECT /*+ INDEX(article idx_created_at) */ * FROM article",
          nativeQuery = true)
   List<Article> optimizedQuery();
   ```

#### ❌ 이런 경우 비추천

- 동적 쿼리 작성 (QueryDSL이 훨씬 나음)
- 여러 곳에서 재사용 (타입 안정성 필요)
- 데이터베이스 변경 가능성
- 복잡한 도메인 로직

---

## 의사결정 프레임워크

### 1단계: 현재 상태 점검

```
현재 JPA + JPQL로 불편한가?
├─ NO → 현상 유지 ✅
└─ YES → 2단계로
```

### 2단계: 불편함의 종류 파악

```
어떤 불편함인가?
├─ 동적 쿼리 작성이 복잡함
│  ├─ 조건 조합이 3개 이상 → QueryDSL ✅
│  └─ 조건 조합이 1~2개 → Service 분기로 충분 ✅
│
├─ 타입 안정성 부족
│  └─ 엔티티 10개 이상 → QueryDSL ✅
│
├─ 복잡한 집계/분석 쿼리
│  ├─ JPA 중심 프로젝트 → QueryDSL (제한적) + Native Query ✅
│  └─ SQL 중심 프로젝트 → JOOQ ✅
│
└─ 데이터베이스 특화 기능 필요
   └─ Native Query ✅
```

### 3단계: 프로젝트 규모 평가

| 규모 | 엔티티 수 | 쿼리 복잡도 | 권장 |
|------|----------|------------|------|
| **소규모** | < 5 | 단순 CRUD | JPA만으로 충분 |
| **소중규모** | 5~10 | 검색 2~3개 | Service 분기 or Specification |
| **중규모** | 10~30 | 복잡한 검색 | QueryDSL |
| **대규모** | 30~100 | 분석 쿼리 | QueryDSL + Native Query |
| **초대규모** | 100+ | 극한 최적화 | JOOQ 고려 |

### 4단계: 비용 대비 효과 분석

```
투자 시간 vs 예상 효과
├─ JOOQ
│  ├─ 투자: 2~3주
│  ├─ 효과: 복잡한 SQL 완벽 제어
│  └─ ROI: 12~18개월 (대규모만 유리)
│
├─ QueryDSL
│  ├─ 투자: 2~4일
│  ├─ 효과: 동적 쿼리 + 타입 안정성
│  └─ ROI: 3~6개월 (중규모 이상 유리)
│
└─ Native Query
   ├─ 투자: 0일 (즉시)
   ├─ 효과: 특정 쿼리 최적화
   └─ ROI: 즉시 (필요할 때만)
```

### 5단계: 팀 역량 고려

```
팀 상황
├─ SQL 전문가 많음 → JOOQ 고려 가능
├─ JPA 익숙 → QueryDSL 추천
├─ 빠른 개발 중시 → 현상 유지 or QueryDSL
└─ 신입 많음 → QueryDSL (학습 곡선 낮음)
```

---

## 실전 사례: 내 프로젝트 분석

### 프로젝트 개요

```yaml
기술 스택:
  - Spring Boot 3.2.4
  - Java 17
  - MySQL 8.0
  - Spring Data JPA

도메인:
  - Member (회원)
  - Article (게시글)

관계:
  - Article N:1 Member
```

### 현재 구현

#### Repository
```java
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 1. 전체 조회
    @Query(value = "SELECT a FROM Article a JOIN FETCH a.author",
           countQuery = "SELECT COUNT(a) FROM Article a")
    Page<Article> findAllWithAuthor(Pageable pageable);

    // 2. 키워드 검색
    @Query(value = "SELECT a FROM Article a JOIN FETCH a.author WHERE " +
                   "a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%",
           countQuery = "SELECT COUNT(a) FROM Article a WHERE " +
                        "a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%")
    Page<Article> findByTitleOrAuthorNameContaining(@Param("keyword") String keyword,
                                                     Pageable pageable);
}
```

#### Service: 애플리케이션 레벨 동적 처리
```java
@Service
public class ArticleService {

    public Page<ArticleResponse> getArticles(String keyword, Pageable pageable) {
        // keyword 유무에 따라 다른 Repository 메서드 호출
        if (keyword != null && !keyword.trim().isEmpty()) {
            return searchArticles(keyword, pageable);  // 검색 쿼리
        }
        return findAllArticles(pageable);  // 전체 조회 쿼리
    }
}
```

### 의사결정 과정

#### 1단계: 불편함이 있는가?

```
✅ 체크리스트
- [X] Fetch Join으로 N+1 문제 해결됨
- [X] 검색 기능 정상 작동
- [X] 페이지네이션 최적화 (countQuery 분리)
- [ ] 동적 쿼리 필요? → keyword 유무만 (1개 조건)
- [ ] 타입 안정성? → 엔티티 2개뿐, 필드명 변경 거의 없음
- [ ] 복잡한 쿼리? → 단순 JOIN + LIKE만
```

**결론**: 큰 불편함 없음

#### 2단계: 미래 요구사항 예측

```
예상 시나리오:
1. Tag 필터 추가 → Service 분기 1줄 추가로 해결 가능
2. 날짜 범위 검색 → Service 분기 1줄 추가로 해결 가능
3. 복잡한 집계 → 발생 가능성 낮음

결론: 조건이 3개 이상 늘어나기 전까지는 현상 유지 가능
```

#### 3단계: 프로젝트 규모 평가

| 지표 | 현재 | 기준 | 평가 |
|------|------|------|------|
| **엔티티 수** | 2개 | < 5개 | 소규모 |
| **검색 조건** | 1개 | < 2개 | 단순 |
| **쿼리 복잡도** | 낮음 | JOIN 1개 | 단순 |
| **팀 규모** | 개인 | - | 소규모 |

**결론**: 소규모 프로젝트

#### 4단계: 비용 대비 효과

```
QueryDSL 도입 시:
- 투자 시간: 2~4일
- 예상 효과:
  ✅ 동적 쿼리 우아하게 작성 (하지만 지금 조건 1개뿐)
  ✅ 타입 안정성 (하지만 엔티티 2개뿐, 변경 드뭄)
  ✅ 가독성 향상 (하지만 지금도 충분히 읽기 쉬움)

현상 유지 시:
- 투자 시간: 0일
- 예상 효과:
  ✅ 기능은 동일하게 작동
  ✅ 단순함 유지
  ✅ 학습 비용 없음

결론: ROI가 낮음 (조건이 더 늘어나면 재고려)
```

#### 5단계: 개선 여지 확인

현재 코드에 작은 개선만 추가:

```java
// keyword가 null일 때 전체 조회 되도록 개선
@Query(value = "SELECT a FROM Article a JOIN FETCH a.author WHERE " +
               "(:keyword IS NULL OR a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%)",
       countQuery = "SELECT COUNT(a) FROM Article a WHERE " +
                    "(:keyword IS NULL OR a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%)")
Page<Article> search(@Param("keyword") String keyword, Pageable pageable);

// Service 단순화
public Page<ArticleResponse> getArticles(String keyword, Pageable pageable) {
    return articleRepository.search(keyword, pageable)
        .map(ArticleResponse::fromEntity);
}
```

**효과**:
- Service 분기 제거
- Repository 메서드 통합 (2개 → 1개)
- **투자 시간: 5분**

### 최종 결정: 현상 유지 (작은 개선)

#### 이유

1. **현재 코드로 충분히 작동**
   - Fetch Join 최적화 ✅
   - 검색 기능 동작 ✅
   - 성능 문제 없음 ✅

2. **프로젝트 규모가 작음**
   - 엔티티 2개
   - 검색 조건 1개
   - 복잡한 쿼리 없음

3. **합리적인 비용**
   - QueryDSL: 2~4일 투자
   - 현재 개선: 5분 투자
   - **투자 대비 효과가 미미함**

4. **미래 확장 시 재고려 가능**
   - 조건 3개 이상 → QueryDSL 고려
   - 엔티티 5개 이상 → QueryDSL 고려
   - 복잡한 집계 → Native Query or JOOQ 고려

### 도입 재고려 시점

```
다음 상황이 발생하면 QueryDSL 도입 검토:

1. 검색 조건 추가
   현재: keyword만
   임계점: Tag + 날짜 범위 + 작성자 등 3개 이상

2. 엔티티 증가
   현재: Member, Article
   임계점: Comment, Like, Category 등 추가 시

3. 조건 재사용
   현재: Article 검색만
   임계점: 3개 이상 도메인에서 같은 검색 로직 필요

4. 팀 확장
   현재: 개인 프로젝트
   임계점: 팀 협업 시작 시
```

---

## 결론: 과하지 않게, 부족하지 않게

### 핵심 메시지

> **"좋은 기술은 많지만, 우리 프로젝트에 맞는 기술은 따로 있다"**

#### 1. 기술은 도구일 뿐

```
QueryDSL이 좋다고 해서 무조건 도입? ❌
JOOQ가 강력하다고 해서 모든 프로젝트에? ❌

현재 불편함이 있는가?
그 불편함이 도구 도입 비용보다 큰가?

이 두 질문에 YES일 때만 도입하세요.
```

#### 2. 단계적 접근

```
1단계: JPA + JPQL (기본)
       → 대부분의 경우 충분

2단계: Service 분기 (애플리케이션 레벨 동적 처리)
       → 조건 1~2개는 이것으로 해결

3단계: Specification API (JPA 기본 기능)
       → 조건 2~3개, 추가 라이브러리 부담 없음

4단계: QueryDSL
       → 조건 3개 이상, 중규모 프로젝트

5단계: JOOQ or Native Query
       → 복잡한 SQL, 대규모 프로젝트
```

#### 3. 실용주의

```
✅ 좋은 의사결정
- 현재 문제를 해결하는 최소한의 도구 선택
- 팀이 이해하고 유지보수할 수 있는 수준
- 비용 대비 효과가 명확

❌ 나쁜 의사결정
- "다른 회사에서 쓴다니까" 도입
- "이력서에 좋을 것 같아서" 도입
- 문제도 없는데 "미래를 위해" 도입
```

### 의사결정 체크리스트

```
□ 현재 코드로 비즈니스 요구사항을 만족하는가?
  ├─ YES → 현상 유지
  └─ NO → 다음 단계

□ 불편함의 종류는?
  ├─ 동적 쿼리 (조건 3개 이상) → QueryDSL
  ├─ 복잡한 SQL → JOOQ or Native Query
  └─ DB 특화 기능 → Native Query

□ 프로젝트 규모는?
  ├─ 엔티티 < 5개 → 현상 유지
  ├─ 엔티티 5~30개 → QueryDSL
  └─ 엔티티 30개+ → QueryDSL + JOOQ 고려

□ 팀이 학습하고 유지보수할 수 있는가?
  ├─ YES → 도입
  └─ NO → 재고려

□ 투자 시간 대비 효과가 명확한가?
  ├─ YES → 도입
  └─ NO → 연기
```

### 각 기술의 Sweet Spot

```
JPA + JPQL
├─ 프로젝트: 소규모 (엔티티 < 5개)
├─ 쿼리: 단순 CRUD + 간단한 검색
└─ 팀: JPA 기본 지식

QueryDSL
├─ 프로젝트: 중규모 (엔티티 10~50개)
├─ 쿼리: 복잡한 동적 검색
└─ 팀: JPA 경험자, 타입 안정성 중시

JOOQ
├─ 프로젝트: 대규모 or 레거시 통합
├─ 쿼리: 복잡한 분석/집계
└─ 팀: SQL 전문가, Database-First

Native Query
├─ 프로젝트: 모든 규모
├─ 쿼리: DB 특화 기능, 일회성 복잡한 쿼리
└─ 팀: SQL 작성 가능
```

### 마지막 조언

#### 초보 개발자에게

```
1. 먼저 JPA를 충분히 익히세요
   - Fetch Join으로 N+1 해결
   - @Query로 JPQL 작성
   - 이것만으로도 대부분 해결됩니다

2. 불편함을 느낄 때까지 기다리세요
   - "미리" 도입하지 마세요
   - 문제가 생기면 그때 배우세요

3. 단계적으로 도입하세요
   - Service 분기 → Specification → QueryDSL
```

#### 중급 개발자에게

```
1. 프로젝트 규모를 객관적으로 평가하세요
   - 엔티티 개수
   - 검색 조건 복잡도
   - 쿼리 패턴 분석

2. 비용을 정직하게 계산하세요
   - 학습 시간
   - 설정 시간
   - 팀 온보딩 시간

3. ROI를 따져보세요
   - Break-even Point가 언제인가?
   - 장기 프로젝트인가, 단기인가?
```

#### 시니어 개발자에게

```
1. 팀의 역량을 고려하세요
   - 신입이 유지보수할 수 있는가?
   - 퇴사 후 인수인계 가능한가?

2. 기술 부채를 관리하세요
   - 지금 도입이 미래에 부담이 될 수 있음
   - 단순함의 가치를 과소평가하지 마세요

3. 대안을 항상 고려하세요
   - 새 기술 도입 vs 현재 코드 개선
   - 어떤 것이 더 효과적인가?
```

---

## 참고 자료

### 공식 문서

- **JOOQ**: https://www.jooq.org/doc/latest/manual/
- **QueryDSL**: http://querydsl.com/
- **Spring Data JPA**: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

### 추가 학습

```
QueryDSL 입문:
1. Spring Data JPA 기본 익히기
2. Custom Repository 패턴 학습
3. QueryDSL 설정 및 기본 쿼리
4. BooleanExpression 동적 쿼리
5. Projection DTO 조회

JOOQ 입문:
1. SQL 고급 문법 학습 (윈도우 함수, CTE)
2. JOOQ Code Generation 설정
3. DSLContext 기본 사용법
4. JPA 통합 전략 수립
5. 트랜잭션 관리 설계
```

---

## 맺으며

이 글을 쓰게 된 계기는 제 프로젝트에 QueryDSL을 도입하려다가,
"정말 필요한가?"라는 질문을 스스로에게 던지면서였습니다.

결론은 **"지금은 아니다"** 였습니다.

하지만 이 과정에서 각 기술을 깊이 분석하고,
언제 필요한지 명확히 이해하게 되었습니다.

**좋은 기술을 아는 것도 중요하지만,
그 기술을 언제 쓰지 말아야 하는지 아는 것이 더 중요합니다.**

여러분의 프로젝트는 어떤가요?
지금 당장 새로운 도구가 필요한가요,
아니면 현재 도구를 잘 쓰는 것만으로도 충분한가요?

이 글이 그 질문에 답하는 데 도움이 되길 바랍니다.

---

**작성일**: 2025-11-03
**저자**: [Your Name]
**프로젝트**: Spring Boot 3.2.4 기반 소규모 웹 애플리케이션
**키워드**: #JPA #QueryDSL #JOOQ #NativeQuery #의사결정 #실용주의

---

## 피드백 환영

이 글에 대한 의견이나 질문이 있으시면 편하게 남겨주세요!
- "우리 프로젝트는 이런 상황인데 어떤 선택이 좋을까요?"
- "이 부분은 다르게 생각하는데요"

모든 피드백은 이 글을 더 나아지게 만듭니다. 😊
