package org.sopt.domain.comment.application.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateRequest(
	@Schema(description = "댓글 내용", example = "수정된 댓글입니다", requiredMode = REQUIRED)
	@NotBlank(message = "댓글 내용을 입력해주세요")
	@Size(max = 300, message = "댓글은 300자 이내로 입력해주세요")
	String content
) {
}
