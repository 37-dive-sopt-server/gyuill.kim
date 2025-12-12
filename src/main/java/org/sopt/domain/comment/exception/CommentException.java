package org.sopt.domain.comment.exception;

import org.sopt.global.exception.base.BaseException;
import org.sopt.global.response.error.ErrorCode;

public class CommentException extends BaseException {

	public CommentException(ErrorCode errorCode) {
		super(errorCode);
	}

	public CommentException(ErrorCode errorCode, String detail) {
		super(errorCode, detail);
	}
}
