package org.sopt.domain.auth.application.service;

import org.sopt.domain.auth.application.dto.TokenPair;
import org.sopt.domain.auth.application.dto.response.LoginResponse;
import org.sopt.domain.auth.exception.AuthException;
import org.sopt.global.auth.jwt.JwtProperties;
import org.sopt.global.auth.oauth2.service.OAuth2TempCodeService;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2AuthService {

	private final OAuth2TempCodeService tempCodeService;
	private final JwtProperties jwtProperties;

	public LoginResponse exchangeOAuth2Code(String code) {
		TokenPair tokens = tempCodeService.consumeCode(code);

		if (tokens == null) {
			throw new AuthException(ErrorCode.TOKEN_INVALID);
		}

		return LoginResponse.of(tokens.accessToken(), tokens.refreshToken(), jwtProperties.getExpiresInSeconds());
	}
}
