package org.sopt.domain.comment.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.annotation.RepositoryTest;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.CommentFixture;
import org.sopt.fixture.MemberFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RepositoryTest
class CommentRepositoryTest {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Member author;
	private Article article;

	@BeforeEach
	void setUp() {
		author = MemberFixture.createLocalMember("author@example.com", "Author");
		author = memberRepository.save(author);

		article = ArticleFixture.createArticle(author, "Test Article");
		article = articleRepository.save(article);
	}

	@Test
	@DisplayName("ID와 작성자 함께 조회 - JOIN FETCH 사용")
	void findByIdWithAuthor_Success() {
		// given
		Comment comment = CommentFixture.createCommentWithContent(article, author, "Test comment");
		Comment saved = commentRepository.save(comment);
		entityManager.clear();

		// when
		Optional<Comment> result = commentRepository.findByIdWithAuthor(saved.getId());

		// then
		assertThat(result).isPresent();
		Comment foundComment = result.get();
		assertThat(foundComment.getContent()).isEqualTo("Test comment");

		// author가 이미 로딩되어 있어야 함 (N+1 문제 없음)
		assertThat(foundComment.getAuthor()).isNotNull();
		assertThat(foundComment.getAuthor().getName()).isEqualTo("Author");
	}

	@Test
	@DisplayName("존재하지 않는 ID로 작성자 함께 조회 시 빈 Optional 반환")
	void findByIdWithAuthor_NotFound() {
		// when
		Optional<Comment> result = commentRepository.findByIdWithAuthor(999L);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("ID와 작성자, 게시글 함께 조회 - JOIN FETCH 사용")
	void findByIdWithDetails_Success() {
		// given
		Comment comment = CommentFixture.createCommentWithContent(article, author, "Detailed comment");
		Comment saved = commentRepository.save(comment);
		entityManager.clear();

		// when
		Optional<Comment> result = commentRepository.findByIdWithDetails(saved.getId());

		// then
		assertThat(result).isPresent();
		Comment foundComment = result.get();
		assertThat(foundComment.getContent()).isEqualTo("Detailed comment");

		// author와 article이 모두 로딩되어 있어야 함
		assertThat(foundComment.getAuthor()).isNotNull();
		assertThat(foundComment.getAuthor().getName()).isEqualTo("Author");
		assertThat(foundComment.getArticle()).isNotNull();
		assertThat(foundComment.getArticle().getTitle()).isEqualTo("Test Article");
	}

	@Test
	@DisplayName("존재하지 않는 ID로 상세 조회 시 빈 Optional 반환")
	void findByIdWithDetails_NotFound() {
		// when
		Optional<Comment> result = commentRepository.findByIdWithDetails(999L);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("작성자 ID로 댓글 목록 조회 - 페이징")
	void findByAuthorIdWithArticle_Pagination() {
		// given
		Member author2 = MemberFixture.createLocalMember("author2@example.com", "Author2");
		author2 = memberRepository.save(author2);

		Article article2 = ArticleFixture.createArticle(author, "Article 2");
		article2 = articleRepository.save(article2);

		// author가 작성한 댓글 3개
		Comment comment1 = CommentFixture.createCommentWithContent(article, author, "Comment 1");
		Comment comment2 = CommentFixture.createCommentWithContent(article2, author, "Comment 2");
		Comment comment3 = CommentFixture.createCommentWithContent(article, author, "Comment 3");

		// author2가 작성한 댓글 1개
		Comment comment4 = CommentFixture.createCommentWithContent(article, author2, "Comment by Author2");

		commentRepository.save(comment1);
		commentRepository.save(comment2);
		commentRepository.save(comment3);
		commentRepository.save(comment4);
		entityManager.clear();

		Pageable pageable = PageRequest.of(0, 2);

		// when
		Page<Comment> result = commentRepository.findByAuthorIdWithArticle(author.getId(), pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.getTotalPages()).isEqualTo(2);

		// article이 이미 로딩되어 있어야 함
		result.getContent().forEach(comment -> {
			assertThat(comment.getArticle()).isNotNull();
		});
	}

	@Test
	@DisplayName("게시글 ID로 댓글 목록 조회 - 작성자 포함")
	void findByArticleIdWithAuthor_Success() {
		// given
		Member author2 = MemberFixture.createLocalMember("author2@example.com", "Author2");
		author2 = memberRepository.save(author2);

		Comment comment1 = CommentFixture.createCommentWithContent(article, author, "First comment");
		Comment comment2 = CommentFixture.createCommentWithContent(article, author2, "Second comment");
		Comment comment3 = CommentFixture.createCommentWithContent(article, author, "Third comment");

		commentRepository.save(comment1);
		commentRepository.save(comment2);
		commentRepository.save(comment3);
		entityManager.clear();

		// when
		List<Comment> result = commentRepository.findByArticleIdWithAuthor(article.getId());

		// then
		assertThat(result).hasSize(3);

		// 모든 댓글의 author가 이미 로딩되어 있어야 함
		result.forEach(comment -> {
			assertThat(comment.getAuthor()).isNotNull();
		});
	}
}
