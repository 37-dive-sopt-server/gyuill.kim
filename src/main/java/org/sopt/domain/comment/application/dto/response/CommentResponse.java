package org.sopt.domain.comment.application.dto.response;

import java.time.LocalDateTime;

import org.sopt.domain.comment.domain.entity.Comment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 정보 응답")
public record CommentResponse(
	@Schema(description = "댓글 ID", example = "1")
	Long id,

	@Schema(description = "게시글 ID", example = "1")
	Long articleId,

	@Schema(description = "게시글 제목", example = "Spring Boot 시작하기")
	String articleTitle,

	@Schema(description = "작성자 ID", example = "1")
	Long authorId,

	@Schema(description = "작성자 이름", example = "김솝트")
	String authorName,

	@Schema(description = "댓글 내용", example = "좋은 글이네요!")
	String content,

	@Schema(description = "생성일시", example = "2024-01-01T10:00:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-01T10:00:00")
	LocalDateTime updatedAt
) {
	public static CommentResponse fromEntity(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getArticle().getId(),
			comment.getArticle().getTitle(),
			comment.getAuthor().getId(),
			comment.getAuthor().getName(),
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}
