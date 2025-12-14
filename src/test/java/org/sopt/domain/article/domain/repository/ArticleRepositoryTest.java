package org.sopt.domain.article.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.MemberFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ArticleRepositoryTest {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Member author;

	@BeforeEach
	void setUp() {
		author = MemberFixture.createLocalMember("author@example.com", "Author");
		author = entityManager.persistAndFlush(author);
	}

	@Test
	@DisplayName("제목으로 게시글 존재 확인 - 존재하는 경우 true")
	void existsByTitle_True() {
		// given
		Article article = ArticleFixture.createArticle(author, "Unique Title");
		entityManager.persistAndFlush(article);
		entityManager.clear();

		// when
		boolean exists = articleRepository.existsByTitle("Unique Title");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("제목으로 게시글 존재 확인 - 존재하지 않는 경우 false")
	void existsByTitle_False() {
		// when
		boolean exists = articleRepository.existsByTitle("Nonexistent Title");

		// then
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("ID와 작성자 함께 조회 - JOIN FETCH 사용")
	void findByIdWithAuthor_Success() {
		// given
		Article article = ArticleFixture.createArticle(author, "Test Article");
		Article saved = entityManager.persistAndFlush(article);
		entityManager.clear(); // 영속성 컨텍스트 초기화

		// when
		Optional<Article> result = articleRepository.findByIdWithAuthor(saved.getId());

		// then
		assertThat(result).isPresent();
		Article foundArticle = result.get();
		assertThat(foundArticle.getTitle()).isEqualTo("Test Article");

		// author가 이미 로딩되어 있어야 함 (N+1 문제 없음)
		assertThat(foundArticle.getAuthor()).isNotNull();
		assertThat(foundArticle.getAuthor().getName()).isEqualTo("Author");
	}

	@Test
	@DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
	void findByIdWithAuthor_NotFound() {
		// when
		Optional<Article> result = articleRepository.findByIdWithAuthor(999L);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("작성자 함께 전체 게시글 조회 - 페이징")
	void findAllWithAuthor_Pagination() {
		// given
		Member author2 = MemberFixture.createLocalMember("author2@example.com", "Author2");
		author2 = entityManager.persistAndFlush(author2);

		Article article1 = ArticleFixture.createArticle(author, "Article 1");
		Article article2 = ArticleFixture.createArticle(author, "Article 2");
		Article article3 = ArticleFixture.createArticle(author2, "Article 3");

		entityManager.persist(article1);
		entityManager.persist(article2);
		entityManager.persist(article3);
		entityManager.flush();
		entityManager.clear();

		Pageable pageable = PageRequest.of(0, 2);

		// when
		Page<Article> result = articleRepository.findAllWithAuthor(pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.getTotalPages()).isEqualTo(2);

		// author가 이미 로딩되어 있어야 함
		result.getContent().forEach(article -> {
			assertThat(article.getAuthor()).isNotNull();
		});
	}

	@Test
	@DisplayName("제목 또는 작성자 이름으로 검색 - 페이징")
	void findByTitleOrAuthorNameContaining_ByTitle() {
		// given
		Article article1 = ArticleFixture.createArticle(author, "Spring Boot Tutorial");
		Article article2 = ArticleFixture.createArticle(author, "Java Programming");
		Article article3 = ArticleFixture.createArticle(author, "Spring Security Guide");

		entityManager.persist(article1);
		entityManager.persist(article2);
		entityManager.persist(article3);
		entityManager.flush();
		entityManager.clear();

		Pageable pageable = PageRequest.of(0, 10);

		// when - 제목에 "Spring" 포함된 게시글 검색
		Page<Article> result = articleRepository.findByTitleOrAuthorNameContaining("Spring", pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);

		result.getContent().forEach(article -> {
			assertThat(article.getTitle()).contains("Spring");
		});
	}

	@Test
	@DisplayName("제목 또는 작성자 이름으로 검색 - 작성자 이름으로 검색")
	void findByTitleOrAuthorNameContaining_ByAuthorName() {
		// given
		Member author2 = MemberFixture.createLocalMember("author2@example.com", "Alice");
		author2 = entityManager.persistAndFlush(author2);

		Article article1 = ArticleFixture.createArticle(author, "Article by Author");
		Article article2 = ArticleFixture.createArticle(author2, "Article by Alice");

		entityManager.persist(article1);
		entityManager.persist(article2);
		entityManager.flush();
		entityManager.clear();

		Pageable pageable = PageRequest.of(0, 10);

		// when - 작성자 이름에 "Alice" 포함된 게시글 검색
		Page<Article> result = articleRepository.findByTitleOrAuthorNameContaining("Alice", pageable);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getAuthor().getName()).contains("Alice");
	}

	@Test
	@DisplayName("검색 결과 없음")
	void findByTitleOrAuthorNameContaining_NoResults() {
		// given
		Article article = ArticleFixture.createArticle(author, "Test Article");
		entityManager.persistAndFlush(article);
		entityManager.clear();

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<Article> result = articleRepository.findByTitleOrAuthorNameContaining("NonExistent", pageable);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
	}

	@Test
	@DisplayName("제목 unique constraint 위반 시 예외 발생")
	void saveWithDuplicateTitle_ThrowsException() {
		// given
		Article article1 = ArticleFixture.createArticle(author, "Duplicate Title");
		entityManager.persistAndFlush(article1);
		entityManager.clear();

		Article article2 = ArticleFixture.createArticle(author, "Duplicate Title");

		// when & then
		assertThatThrownBy(() -> {
			entityManager.persistAndFlush(article2);
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("게시글 저장 및 ID 자동 생성 확인")
	void save_GeneratesId() {
		// given
		Article article = ArticleFixture.createArticle(author, "New Article");

		// when
		Article saved = articleRepository.save(article);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getTitle()).isEqualTo("New Article");
		assertThat(saved.getAuthor()).isEqualTo(author);
	}
}
