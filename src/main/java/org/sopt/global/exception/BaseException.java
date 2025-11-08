package org.sopt.global.exception;

import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.error.ErrorType;

public class BaseException extends RuntimeException {
	private final ErrorType errorCode;

	public BaseException(ErrorType errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BaseException(ErrorCode errorCode, String detail) {
		super(errorCode.getMessage() + " → " + detail);
		this.errorCode = errorCode;
	}

	public ErrorType getErrorCode() {
		return errorCode;
	}
}