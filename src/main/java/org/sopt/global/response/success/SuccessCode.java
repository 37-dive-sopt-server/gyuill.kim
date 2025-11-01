package org.sopt.global.response.success;

public enum SuccessCode implements SuccessType {
	SUCCESS("S200", "성공"),

	MEMBER_EMAIL_CHECK_OK("M201", "이메일 사용 가능"),
	MEMBER_CREATED("M202", "회원가입 성공"),
	MEMBER_DELETED("M203", "회원 삭제 성공"),
	MEMBER_VIEW("M204", "회원 정보 조회 성공");

	private final String code;
	private final String message;

	SuccessCode(String code, String message) {
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
