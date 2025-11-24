package org.sopt.domain.article.application.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import org.sopt.domain.article.domain.entity.Tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시글 작성 요청")
public record ArticleCreateRequest(
	@Schema(description = "작성자 ID", example = "1", requiredMode = REQUIRED)
	@NotNull(message = "작성자 ID를 입력해주세요")
	Long authorId,

	@Schema(description = "게시글 제목", example = "Spring Boot 시작하기", requiredMode = REQUIRED)
	@NotBlank(message = "제목을 입력해주세요")
	String title,

	@Schema(description = "게시글 내용", example = "Spring Boot는...", requiredMode = REQUIRED)
	@NotBlank(message = "내용을 입력해주세요")
	String content,

	@Schema(description = "태그 (CS/DB/SPRING/ETC)", example = "SPRING", requiredMode = REQUIRED)
	@NotNull(message = "태그를 선택해주세요")
	Tag tag
) {
}
