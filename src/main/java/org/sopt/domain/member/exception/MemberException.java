package org.sopt.domain.member.exception;

import org.sopt.global.exception.BaseException;
import org.sopt.global.response.error.ErrorCode;

public class MemberException extends BaseException {

	public MemberException(ErrorCode errorCode) {
		super(errorCode);
	}

	public MemberException(ErrorCode errorCode, String detail) {
		super(errorCode, detail);
	}
}