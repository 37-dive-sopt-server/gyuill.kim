package org.sopt.domain.comment.domain.service;

import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.exception.CommentException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommentValidator {

	private static final int MAX_CONTENT_LENGTH = 300;

	public void validateContent(String content) {
		if (content == null || content.trim().isEmpty()) {
			throw new CommentException(ErrorCode.COMMENT_CONTENT_REQUIRED);
		}
		if (content.length() > MAX_CONTENT_LENGTH) {
			throw new CommentException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
		}
	}

	public void validateAuthorization(Comment comment, Long requesterId) {
		if (!comment.isAuthor(requesterId)) {
			throw new CommentException(ErrorCode.COMMENT_UNAUTHORIZED);
		}
	}
}
