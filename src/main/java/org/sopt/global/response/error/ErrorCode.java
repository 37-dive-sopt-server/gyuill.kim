package org.sopt.global.response.error;

public enum ErrorCode implements ErrorType {

	// 공통 에러
	INVALID_INPUT("C001", "입력값이 올바르지 않습니다", 400),
	INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다", 400),

	INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다", 500),

	// 인증/인가 에러
	LOGIN_FAIL("A401", "이메일 또는 비밀번호가 틀렸습니다", 401),
	TOKEN_MISSING("A402", "토큰이 존재하지 않습니다", 401),
	TOKEN_INVALID("A403", "유효하지 않은 토큰입니다", 401),
	TOKEN_EXPIRED("A404", "만료된 토큰입니다", 401),
	TOKEN_BLACKLISTED("A405", "블랙리스트 처리된 토큰입니다", 401),
	REFRESH_TOKEN_NOT_FOUND("A406", "Refresh Token을 찾을 수 없습니다", 404),

	// 회원 관련 에러
	MEMBER_NOT_FOUND("M401", "회원을 찾을 수 없습니다", 404),
	DUPLICATE_EMAIL("M402", "이미 가입된 이메일입니다", 400),
	BIRTH_DATE_REQUIRED("M404", "생년월일을 입력해주세요", 400),
	BIRTH_DATE_FUTURE("M405", "생년월일은 과거 날짜여야 합니다", 400),
	AGE_UNDER_20("M406", "20세 미만은 회원 가입이 불가능합니다", 400),

	// 게시글 관련 에러
	ARTICLE_NOT_FOUND("A401", "게시글을 찾을 수 없습니다", 404),
	DUPLICATE_ARTICLE_TITLE("A403", "이미 존재하는 게시글 제목입니다", 400),

	// 댓글 관련 에러
	COMMENT_NOT_FOUND("C401", "댓글을 찾을 수 없습니다", 404),
	COMMENT_CONTENT_REQUIRED("C402", "댓글 내용을 입력해주세요", 400),
	COMMENT_CONTENT_TOO_LONG("C403", "댓글은 300자 이내로 입력해주세요", 400),
	COMMENT_UNAUTHORIZED("C404", "댓글 작성자만 수정/삭제할 수 있습니다", 403);

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
