package org.sopt.domain.comment.presentation.controller;

import org.sopt.domain.comment.application.dto.request.CommentCreateRequest;
import org.sopt.domain.comment.application.dto.request.CommentUpdateRequest;
import org.sopt.domain.comment.application.dto.response.CommentResponse;
import org.sopt.domain.comment.application.service.CommentService;
import org.sopt.global.annotation.AutoApiResponse;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.auth.security.CustomUserDetails;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@AutoApiResponse
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관리 API")
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	@SuccessCodeAnnotation(SuccessCode.COMMENT_CREATED)
	@Operation(summary = "댓글 작성", description = "현재 로그인한 사용자가 게시글에 댓글을 작성합니다.")
	public CommentResponse createComment(
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
		@Parameter(description = "댓글 작성 정보", required = true)
		@Valid @RequestBody CommentCreateRequest request
	) {
		return commentService.create(userDetails.getMemberId(), request);
	}

	@GetMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.COMMENT_VIEW)
	@Operation(summary = "댓글 조회", description = "ID로 특정 댓글의 정보를 조회합니다.")
	public CommentResponse getCommentById(
		@Parameter(description = "댓글 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		return commentService.getCommentById(id);
	}

	@GetMapping("/me")
	@SuccessCodeAnnotation(SuccessCode.COMMENT_VIEW)
	@Operation(summary = "내 댓글 조회", description = "현재 로그인한 사용자가 작성한 모든 댓글을 조회합니다.")
	public Page<CommentResponse> getMyComments(
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return commentService.getMyComments(userDetails.getMemberId(), pageable);
	}

	@PutMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.COMMENT_UPDATED)
	@Operation(summary = "댓글 수정", description = "댓글 작성자만 자신의 댓글을 수정할 수 있습니다.")
	public CommentResponse updateComment(
		@Parameter(description = "댓글 ID", required = true, example = "1")
		@PathVariable Long id,
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
		@Parameter(description = "댓글 수정 정보", required = true)
		@Valid @RequestBody CommentUpdateRequest request
	) {
		return commentService.update(id, userDetails.getMemberId(), request);
	}

	@DeleteMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.COMMENT_DELETED)
	@Operation(summary = "댓글 삭제", description = "댓글 작성자만 자신의 댓글을 삭제할 수 있습니다.")
	public void deleteComment(
		@Parameter(description = "댓글 ID", required = true, example = "1")
		@PathVariable Long id,
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		commentService.delete(id, userDetails.getMemberId());
	}
}
