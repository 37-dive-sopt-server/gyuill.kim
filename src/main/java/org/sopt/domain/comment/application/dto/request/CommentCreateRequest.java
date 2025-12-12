package org.sopt.domain.comment.application.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 작성 요청")
public record CommentCreateRequest(
	@Schema(description = "게시글 ID", example = "1", requiredMode = REQUIRED)
	@NotNull(message = "게시글 ID를 입력해주세요")
	Long articleId,

	@Schema(description = "댓글 내용", example = "좋은 글이네요!", requiredMode = REQUIRED)
	@NotBlank(message = "댓글 내용을 입력해주세요")
	@Size(max = 300, message = "댓글은 300자 이내로 입력해주세요")
	String content
) {
}
