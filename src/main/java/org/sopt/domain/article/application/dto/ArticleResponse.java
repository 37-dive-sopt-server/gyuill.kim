package org.sopt.domain.article.application.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.comment.application.dto.response.CommentSimpleResponse;
import org.sopt.domain.comment.domain.entity.Comment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 정보 응답")
public record ArticleResponse(
	@Schema(description = "게시글 ID", example = "1")
	Long id,

	@Schema(description = "작성자 ID", example = "1")
	Long authorId,

	@Schema(description = "작성자 이름", example = "김솝트")
	String authorName,

	@Schema(description = "게시글 제목", example = "Spring Boot 시작하기")
	String title,

	@Schema(description = "게시글 내용", example = "Spring Boot는...")
	String content,

	@Schema(description = "태그", example = "SPRING")
	Tag tag,

	@Schema(description = "생성일시", example = "2024-01-01T10:00:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-01T10:00:00")
	LocalDateTime updatedAt,

	@Schema(description = "댓글 목록")
	List<CommentSimpleResponse> comments
) {
	public static ArticleResponse fromEntity(Article article) {
		return new ArticleResponse(
			article.getId(),
			article.getAuthor().getId(),
			article.getAuthor().getName(),
			article.getTitle(),
			article.getContent(),
			article.getTag(),
			article.getCreatedAt(),
			article.getUpdatedAt(),
			Collections.emptyList()
		);
	}

	public static ArticleResponse fromEntityWithComments(Article article, List<Comment> comments) {
		return new ArticleResponse(
			article.getId(),
			article.getAuthor().getId(),
			article.getAuthor().getName(),
			article.getTitle(),
			article.getContent(),
			article.getTag(),
			article.getCreatedAt(),
			article.getUpdatedAt(),
			comments.stream()
				.map(CommentSimpleResponse::fromEntity)
				.toList()
		);
	}
}
