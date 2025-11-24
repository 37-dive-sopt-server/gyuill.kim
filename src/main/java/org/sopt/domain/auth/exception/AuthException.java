package org.sopt.domain.auth.exception;

import org.sopt.global.exception.BaseException;
import org.sopt.global.response.error.ErrorCode;

public class AuthException extends BaseException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
