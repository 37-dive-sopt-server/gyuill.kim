package org.sopt.global.exception;

import org.sopt.global.response.error.ErrorType;

public class DataAccessException extends BaseException {

	public DataAccessException(ErrorType errorCode) {
		super(errorCode);
	}
}
