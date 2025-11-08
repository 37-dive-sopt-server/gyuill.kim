package org.sopt.global.response.error;

public enum ErrorCode implements ErrorType {

	// 회원 관련 에러
	MEMBER_NOT_FOUND("M401", "회원을 찾을 수 없습니다", 404),
	DUPLICATE_EMAIL("M402", "이미 가입된 이메일입니다", 400),
	BIRTH_DATE_REQUIRED("M404", "생년월일을 입력해주세요", 400),
	BIRTH_DATE_FUTURE("M405", "생년월일은 과거 날짜여야 합니다", 400),
	AGE_UNDER_20("M406", "20세 미만은 회원 가입이 불가능합니다", 400),

	// 게시글 관련 에러
	ARTICLE_NOT_FOUND("A401", "게시글을 찾을 수 없습니다", 404),
	DUPLICATE_ARTICLE_TITLE("A403", "이미 존재하는 게시글 제목입니다", 400),

	// 공통 에러
	INVALID_INPUT("C001", "입력값이 올바르지 않습니다", 400),
	INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다", 400),

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
