package org.sopt.global.response.error;

public enum ErrorCode implements ErrorType {

	// 회원 관련 에러
	MEMBER_NOT_FOUND("M401", "회원을 찾을 수 없습니다"),
	DUPLICATE_EMAIL("M402", "이미 가입된 이메일입니다"),

	// 공통 에러
	INVALID_INPUT("C001", "입력값이 올바르지 않습니다"),
	INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다"),
	INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다");

	private final String code;
	private final String message;

	ErrorCode(String code, String message) {
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
