package org.sopt.global.response.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements ErrorType {

	MEMBER_NOT_FOUND("M401", "회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()),
	DUPLICATE_EMAIL("M401", "이미 가입된 이메일입니다", HttpStatus.CONFLICT.value());

	private final String code;
	private final String message;

	ErrorCode(String code, String message, int status) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
