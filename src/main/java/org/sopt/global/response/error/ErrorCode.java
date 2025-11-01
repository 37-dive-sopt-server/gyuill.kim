package org.sopt.global.response.error;

public enum ErrorCode implements ErrorType {

	// 회원 관련 에러
	MEMBER_NOT_FOUND("M401", "회원을 찾을 수 없습니다", 404),
	DUPLICATE_EMAIL("M402", "이미 가입된 이메일입니다", 400),

	// 공통 에러
	INVALID_INPUT("C001", "입력값이 올바르지 않습니다", 400),
	INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다", 400),

	// 데이터 접근 에러
	DATA_READ_ERROR("C003", "데이터를 읽을 수 없습니다", 500),
	DATA_PARSE_ERROR("C004", "데이터 형식을 파싱할 수 없습니다", 500),
	DATA_WRITE_ERROR("C005", "데이터를 저장할 수 없습니다", 500),
	DATA_DELETE_ERROR("C006", "데이터를 삭제할 수 없습니다", 500),
	DATA_MOVE_ERROR("C007", "임시 파일을 원본 파일로 이동할 수 없습니다", 500),

	INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다", 500);

	private final String code;
	private final String message;
	private final int status;

	ErrorCode(String code, String message, int status) {
		this.code = code;
		this.message = message;
		this.status = status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getStatus() {
		return status;
	}

}
