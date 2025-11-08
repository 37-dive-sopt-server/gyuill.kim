package org.sopt.domain.article.exception;

import org.sopt.global.exception.BaseException;
import org.sopt.global.response.error.ErrorCode;

public class ArticleException extends BaseException {

	public ArticleException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ArticleException(ErrorCode errorCode, String detail) {
		super(errorCode, detail);
	}
}
