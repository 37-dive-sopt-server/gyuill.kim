package org.sopt.global.exception;

/**
 * 데이터 저장소 접근 실패
 */
public class DataAccessException extends RuntimeException {

	public DataAccessException(String message) {
		super(message);
	}

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}
}
