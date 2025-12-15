package org.sopt.domain.article.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.domain.article.application.dto.ArticleCreateRequest;
import org.sopt.domain.article.application.dto.ArticleResponse;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.domain.repository.CommentRepository;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.CommentFixture;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

	@Mock
	private ArticleRepository articleRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private ArticleService articleService;

	@Test
	@DisplayName("게시글 생성 성공")
	void create_Success() {
		// given
		Long authorId = 1L;
		ArticleCreateRequest request = new ArticleCreateRequest(
			"Test Article",
			"Test content",
			Tag.CS
		);

		Member author = MemberFixture.createMemberWithId(authorId, "author@example.com", "Author");
		Article article = ArticleFixture.createArticle(author, request.title());

		given(articleRepository.existsByTitle(request.title())).willReturn(false);
		given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
		given(articleRepository.save(any(Article.class))).willReturn(article);

		// when
		ArticleResponse response = articleService.create(authorId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo(request.title());

		verify(articleRepository).existsByTitle(request.title());
		verify(memberRepository).findById(authorId);
		verify(articleRepository).save(any(Article.class));
	}

	@Test
	@DisplayName("게시글 생성 실패 - 제목 중복")
	void create_DuplicateTitle() {
		// given
		Long authorId = 1L;
		ArticleCreateRequest request = new ArticleCreateRequest(
			"Duplicate Title",
			"Test content",
			Tag.CS
		);

		given(articleRepository.existsByTitle(request.title())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> articleService.create(authorId, request))
			.isInstanceOf(ArticleException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ARTICLE_TITLE);

		verify(articleRepository).existsByTitle(request.title());
		verify(memberRepository, never()).findById(anyLong());
		verify(articleRepository, never()).save(any());
	}

	@Test
	@DisplayName("게시글 생성 실패 - 작성자를 찾을 수 없음")
	void create_AuthorNotFound() {
		// given
		Long authorId = 999L;
		ArticleCreateRequest request = new ArticleCreateRequest(
			"Test Article",
			"Test content",
			Tag.CS
		);

		given(articleRepository.existsByTitle(request.title())).willReturn(false);
		given(memberRepository.findById(authorId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> articleService.create(authorId, request))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

		verify(articleRepository).existsByTitle(request.title());
		verify(memberRepository).findById(authorId);
		verify(articleRepository, never()).save(any());
	}

	@Test
	@DisplayName("게시글 상세 조회 성공 - 댓글 포함")
	void getArticleById_Success() {
		// given
		Long articleId = 1L;
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(articleId, author, "Test Article");

		Member commentAuthor = MemberFixture.createMemberWithId(2L, "commenter@example.com", "Commenter");
		Comment comment1 = CommentFixture.createComment(article, commentAuthor);
		Comment comment2 = CommentFixture.createComment(article, author);

		given(articleRepository.findByIdWithAuthor(articleId)).willReturn(Optional.of(article));
		given(commentRepository.findByArticleIdWithAuthor(articleId)).willReturn(List.of(comment1, comment2));

		// when
		ArticleResponse response = articleService.getArticleById(articleId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo(article.getTitle());

		verify(articleRepository).findByIdWithAuthor(articleId);
		verify(commentRepository).findByArticleIdWithAuthor(articleId);
	}

	@Test
	@DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
	void getArticleById_NotFound() {
		// given
		Long articleId = 999L;
		given(articleRepository.findByIdWithAuthor(articleId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> articleService.getArticleById(articleId))
			.isInstanceOf(ArticleException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);

		verify(articleRepository).findByIdWithAuthor(articleId);
		verify(commentRepository, never()).findByArticleIdWithAuthor(anyLong());
	}

	@Test
	@DisplayName("게시글 목록 조회 - 검색어 없음 (전체 조회)")
	void getArticles_WithoutKeyword() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article1 = ArticleFixture.createArticle(author, "Article 1");
		Article article2 = ArticleFixture.createArticle(author, "Article 2");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Article> articlePage = new PageImpl<>(List.of(article1, article2), pageable, 2);

		given(articleRepository.findAllWithAuthor(pageable)).willReturn(articlePage);

		// when
		Page<ArticleResponse> result = articleService.getArticles(null, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);

		verify(articleRepository).findAllWithAuthor(pageable);
		verify(articleRepository, never()).findByTitleOrAuthorNameContaining(anyString(), any());
	}

	@Test
	@DisplayName("게시글 목록 조회 - 검색어 있음 (검색)")
	void getArticles_WithKeyword() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article1 = ArticleFixture.createArticle(author, "Spring Boot Tutorial");
		Article article2 = ArticleFixture.createArticle(author, "Spring Security Guide");

		String keyword = "Spring";
		Pageable pageable = PageRequest.of(0, 10);
		Page<Article> articlePage = new PageImpl<>(List.of(article1, article2), pageable, 2);

		given(articleRepository.findByTitleOrAuthorNameContaining(keyword, pageable)).willReturn(articlePage);

		// when
		Page<ArticleResponse> result = articleService.getArticles(keyword, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);

		verify(articleRepository).findByTitleOrAuthorNameContaining(keyword, pageable);
		verify(articleRepository, never()).findAllWithAuthor(any());
	}

	@Test
	@DisplayName("게시글 목록 조회 - 빈 검색어 (전체 조회)")
	void getArticles_WithEmptyKeyword() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticle(author, "Article");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Article> articlePage = new PageImpl<>(List.of(article), pageable, 1);

		given(articleRepository.findAllWithAuthor(pageable)).willReturn(articlePage);

		// when
		Page<ArticleResponse> result = articleService.getArticles("  ", pageable);

		// then
		assertThat(result.getContent()).hasSize(1);

		verify(articleRepository).findAllWithAuthor(pageable);
		verify(articleRepository, never()).findByTitleOrAuthorNameContaining(anyString(), any());
	}

	@Test
	@DisplayName("게시글 검색 결과 없음")
	void getArticles_NoResults() {
		// given
		String keyword = "NonExistent";
		Pageable pageable = PageRequest.of(0, 10);
		Page<Article> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		given(articleRepository.findByTitleOrAuthorNameContaining(keyword, pageable)).willReturn(emptyPage);

		// when
		Page<ArticleResponse> result = articleService.getArticles(keyword, pageable);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();

		verify(articleRepository).findByTitleOrAuthorNameContaining(keyword, pageable);
	}
}
