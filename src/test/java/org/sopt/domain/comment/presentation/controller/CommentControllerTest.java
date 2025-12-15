package org.sopt.domain.comment.presentation.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.config.MockArgumentResolver;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.comment.application.dto.request.CommentCreateRequest;
import org.sopt.domain.comment.application.dto.request.CommentUpdateRequest;
import org.sopt.domain.comment.application.dto.response.CommentResponse;
import org.sopt.domain.comment.application.service.CommentService;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.exception.CommentException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.CommentFixture;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.exception.GlobalExceptionHandler;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@InjectMocks
	private CommentController commentController;

	@Mock
	private CommentService commentService;

	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		mockMvc = MockMvcBuilders
			.standaloneSetup(commentController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.setCustomArgumentResolvers(
				new MockArgumentResolver(),
				new PageableHandlerMethodArgumentResolver()
			)
			.build();
	}

	@Test
	@DisplayName("댓글 작성 성공")
	void createComment_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		CommentCreateRequest request = new CommentCreateRequest(100L, "Test comment");

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(1L, article, author);
		CommentResponse response = CommentResponse.fromEntity(comment);

		given(commentService.create(eq(1L), any(CommentCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/comments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value(comment.getContent()))
			.andExpect(jsonPath("$.authorName").value("Author"));

		verify(commentService).create(eq(1L), any(CommentCreateRequest.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 잘못된 요청 (빈 내용)")
	void createComment_InvalidContent() throws Exception {
		// given
		CommentCreateRequest request = new CommentCreateRequest(100L, "");  // 빈 내용

		// when & then
		mockMvc.perform(post("/comments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(commentService, never()).create(anyLong(), any());
	}

	@Test
	@DisplayName("댓글 조회 성공")
	void getCommentById_Success() throws Exception {
		// given
		Long commentId = 1L;
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);
		CommentResponse response = CommentResponse.fromEntity(comment);

		given(commentService.getCommentById(commentId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/comments/{id}", commentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(commentId))
			.andExpect(jsonPath("$.content").value(comment.getContent()))
			.andExpect(jsonPath("$.authorName").value("Author"));

		verify(commentService).getCommentById(commentId);
	}

	@Test
	@DisplayName("댓글 조회 실패 - 존재하지 않는 댓글")
	void getCommentById_NotFound() throws Exception {
		// given
		Long commentId = 999L;
		given(commentService.getCommentById(commentId))
			.willThrow(new CommentException(ErrorCode.COMMENT_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/comments/{id}", commentId))
			.andExpect(status().isNotFound());

		verify(commentService).getCommentById(commentId);
	}

	@Test
	@DisplayName("내 댓글 조회 성공")
	void getMyComments_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment1 = CommentFixture.createCommentWithId(1L, article, author);
		Comment comment2 = CommentFixture.createCommentWithId(2L, article, author);

		CommentResponse response1 = CommentResponse.fromEntity(comment1);
		CommentResponse response2 = CommentResponse.fromEntity(comment2);

		Pageable pageable = PageRequest.of(0, 20);
		Page<CommentResponse> page = new PageImpl<>(List.of(response1, response2), pageable, 2);

		given(commentService.getMyComments(eq(1L), any(Pageable.class))).willReturn(page);

		// when & then
		mockMvc.perform(get("/comments/me")
				.param("page", "0")
				.param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.totalElements").value(2));

		verify(commentService).getMyComments(eq(1L), any(Pageable.class));
	}

	@Test
	@DisplayName("댓글 수정 성공")
	void updateComment_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Long commentId = 1L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
		Comment comment = CommentFixture.createCommentWithId(commentId, article, author);
		CommentResponse response = CommentResponse.fromEntity(comment);

		given(commentService.update(eq(commentId), eq(1L), any(CommentUpdateRequest.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(put("/comments/{id}", commentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(commentId));

		verify(commentService).update(eq(commentId), eq(1L), any(CommentUpdateRequest.class));
	}

	@Test
	@DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
	void updateComment_NotFound() throws Exception {
		// given
		Long commentId = 999L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		given(commentService.update(eq(commentId), eq(1L), any(CommentUpdateRequest.class)))
			.willThrow(new CommentException(ErrorCode.COMMENT_NOT_FOUND));

		// when & then
		mockMvc.perform(put("/comments/{id}", commentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());

		verify(commentService).update(eq(commentId), eq(1L), any(CommentUpdateRequest.class));
	}

	@Test
	@DisplayName("댓글 수정 실패 - 작성자가 아님")
	void updateComment_Unauthorized() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Long commentId = 1L;
		CommentUpdateRequest request = new CommentUpdateRequest("Updated content");

		given(commentService.update(eq(commentId), eq(1L), any(CommentUpdateRequest.class)))
			.willThrow(new CommentException(ErrorCode.COMMENT_UNAUTHORIZED));

		// when & then
		mockMvc.perform(put("/comments/{id}", commentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());

		verify(commentService).update(eq(commentId), eq(1L), any(CommentUpdateRequest.class));
	}

	@Test
	@DisplayName("댓글 삭제 성공")
	void deleteComment_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Long commentId = 1L;

		willDoNothing().given(commentService).delete(commentId, 1L);

		// when & then
		mockMvc.perform(delete("/comments/{id}", commentId))
			.andExpect(status().isOk());

		verify(commentService).delete(commentId, 1L);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
	void deleteComment_NotFound() throws Exception {
		// given
		Long commentId = 999L;

		willThrow(new CommentException(ErrorCode.COMMENT_NOT_FOUND))
			.given(commentService).delete(commentId, 1L);

		// when & then
		mockMvc.perform(delete("/comments/{id}", commentId))
			.andExpect(status().isNotFound());

		verify(commentService).delete(commentId, 1L);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 작성자가 아님")
	void deleteComment_Unauthorized() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Long commentId = 1L;

		willThrow(new CommentException(ErrorCode.COMMENT_UNAUTHORIZED))
			.given(commentService).delete(commentId, 1L);

		// when & then
		mockMvc.perform(delete("/comments/{id}", commentId))
			.andExpect(status().isForbidden());

		verify(commentService).delete(commentId, 1L);
	}
}
