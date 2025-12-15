package org.sopt.domain.comment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.comment.application.dto.request.CommentCreateRequest;
import org.sopt.domain.comment.application.dto.request.CommentUpdateRequest;
import org.sopt.domain.comment.application.dto.response.CommentResponse;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.domain.repository.CommentRepository;
import org.sopt.domain.comment.exception.CommentException;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private ArticleRepository articleRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private CommentService commentService;

	@Test
	@DisplayName("댓글 생성 성공")
	void create_Success() {
		// given
		Long authorId = 1L;
		Long articleId = 100L;
		CommentCreateRequest request = new CommentCreateRequest(articleId, "Test comment");

		Member author = MemberFixture.createMemberWithId(authorId, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(articleId, author, "Test Article");
		Comment comment = CommentFixture.createComment(article, author);

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
		given(commentRepository.save(any(Comment.class))).willReturn(comment);

		// when
		CommentResponse response = commentService.create(authorId, request);

		// then
		assertThat(response).isNotNull();

		verify(articleRepository).findById(articleId);
		verify(memberRepository).findById(authorId);
		verify(commentRepository).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 생성 실패 - 게시글을 찾을 수 없음")
	void create_ArticleNotFound() {
		// given
		Long authorId = 1L;
		Long articleId = 999L;
		CommentCreateRequest request = new CommentCreateRequest(articleId, "Test comment");

		given(articleRepository.findById(articleId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.create(authorId, request))
			.isInstanceOf(ArticleException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);

		verify(articleRepository).findById(articleId);
		verify(memberRepository, never()).findById(anyLong());
		verify(commentRepository, never()).save(any());
	}

	@Test
	@DisplayName("댓글 생성 실패 - 작성자를 찾을 수 없음")
	void create_AuthorNotFound() {
		// given
		Long authorId = 999L;
		Long articleId = 100L;
		CommentCreateRequest request = new CommentCreateRequest(articleId, "Test comment");

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(articleId, author, "Test Article");

		given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
		given(memberRepository.findById(authorId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.create(authorId, request))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

		verify(articleRepository).findById(articleId);
		verify(memberRepository).findById(authorId);
		verify(commentRepository, never()).save(any());
	}

	@Test
	@DisplayName("댓글 조회 성공")
	void getCommentById_Success() {
		// given
		Long commentId = 1L;
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);

		given(commentRepository.findByIdWithDetails(commentId)).willReturn(Optional.of(comment));

		// when
		CommentResponse response = commentService.getCommentById(commentId);

		// then
		assertThat(response).isNotNull();

		verify(commentRepository).findByIdWithDetails(commentId);
	}

	@Test
	@DisplayName("댓글 조회 실패 - 존재하지 않는 댓글")
	void getCommentById_NotFound() {
		// given
		Long commentId = 999L;
		given(commentRepository.findByIdWithDetails(commentId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.getCommentById(commentId))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);

		verify(commentRepository).findByIdWithDetails(commentId);
	}

	@Test
	@DisplayName("내가 작성한 댓글 목록 조회")
	void getMyComments_Success() {
		// given
		Long authorId = 1L;
		Member author = MemberFixture.createMemberWithId(authorId, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment1 = CommentFixture.createComment(article, author);
		Comment comment2 = CommentFixture.createComment(article, author);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Comment> commentPage = new PageImpl<>(List.of(comment1, comment2), pageable, 2);

		given(commentRepository.findByAuthorIdWithArticle(authorId, pageable)).willReturn(commentPage);

		// when
		Page<CommentResponse> result = commentService.getMyComments(authorId, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);

		verify(commentRepository).findByAuthorIdWithArticle(authorId, pageable);
	}

	@Test
	@DisplayName("댓글 수정 성공")
	void update_Success() {
		// given
		Long commentId = 1L;
		Long requesterId = 1L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		Member author = MemberFixture.createMemberWithId(requesterId, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);

		given(commentRepository.findByIdWithDetails(commentId)).willReturn(Optional.of(comment));

		// when
		CommentResponse response = commentService.update(commentId, requesterId, request);

		// then
		assertThat(response).isNotNull();

		verify(commentRepository).findByIdWithDetails(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
	void update_NotFound() {
		// given
		Long commentId = 999L;
		Long requesterId = 1L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		given(commentRepository.findByIdWithDetails(commentId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.update(commentId, requesterId, request))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);

		verify(commentRepository).findByIdWithDetails(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 작성자가 아님")
	void update_Unauthorized() {
		// given
		Long commentId = 1L;
		Long requesterId = 999L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);

		given(commentRepository.findByIdWithDetails(commentId)).willReturn(Optional.of(comment));

		// when & then
		assertThatThrownBy(() -> commentService.update(commentId, requesterId, request))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_UNAUTHORIZED);

		verify(commentRepository).findByIdWithDetails(commentId);
	}

	@Test
	@DisplayName("댓글 삭제 성공")
	void delete_Success() {
		// given
		Long commentId = 1L;
		Long requesterId = 1L;

		Member author = MemberFixture.createMemberWithId(requesterId, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);

		given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));
		willDoNothing().given(commentRepository).delete(comment);

		// when
		commentService.delete(commentId, requesterId);

		// then
		verify(commentRepository).findByIdWithAuthor(commentId);
		verify(commentRepository).delete(comment);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
	void delete_NotFound() {
		// given
		Long commentId = 999L;
		Long requesterId = 1L;

		given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.delete(commentId, requesterId))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);

		verify(commentRepository).findByIdWithAuthor(commentId);
		verify(commentRepository, never()).delete(any());
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 작성자가 아님")
	void delete_Unauthorized() {
		// given
		Long commentId = 1L;
		Long requesterId = 999L;

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);

		given(commentRepository.findByIdWithAuthor(commentId)).willReturn(Optional.of(comment));

		// when & then
		assertThatThrownBy(() -> commentService.delete(commentId, requesterId))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_UNAUTHORIZED);

		verify(commentRepository).findByIdWithAuthor(commentId);
		verify(commentRepository, never()).delete(any());
	}
}
