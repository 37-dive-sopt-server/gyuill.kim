package org.sopt.domain.auth.presentation.controller;

import org.sopt.domain.auth.application.dto.request.LoginRequest;
import org.sopt.domain.auth.application.dto.response.LoginResponse;
import org.sopt.domain.auth.application.dto.request.TokenRefreshRequest;
import org.sopt.domain.auth.application.dto.response.TokenRefreshResponse;
import org.sopt.domain.auth.application.service.AuthService;
import org.sopt.domain.auth.application.service.OAuth2AuthService;
import org.sopt.global.annotation.ApiExceptions;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.CommonApiResponse;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증 API", description = "로그인, 토큰 갱신, 로그아웃 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final OAuth2AuthService oauth2AuthService;

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Access Token과 Refresh Token을 발급받습니다.")
	@ApiExceptions({
		ErrorCode.LOGIN_FAIL,
		ErrorCode.MEMBER_NOT_FOUND,
		ErrorCode.INVALID_INPUT
	})
	@SuccessCodeAnnotation(SuccessCode.LOGIN_SUCCESS)
	@PostMapping("/login")
	public CommonApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return CommonApiResponse.success(SuccessCode.LOGIN_SUCCESS, response);
	}

	@Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
	@ApiExceptions({
		ErrorCode.TOKEN_INVALID,
		ErrorCode.TOKEN_EXPIRED,
		ErrorCode.TOKEN_BLACKLISTED,
		ErrorCode.REFRESH_TOKEN_NOT_FOUND,
		ErrorCode.MEMBER_NOT_FOUND
	})
	@SuccessCodeAnnotation(SuccessCode.TOKEN_REFRESH_SUCCESS)
	@PostMapping("/refresh")
	public CommonApiResponse<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		TokenRefreshResponse response = authService.renewalRefreshToken(request);
		return CommonApiResponse.success(SuccessCode.TOKEN_REFRESH_SUCCESS, response);
	}

	@Operation(summary = "로그아웃", description = "Refresh Token을 블랙리스트에 등록하여 로그아웃합니다.")
	@ApiExceptions({
		ErrorCode.REFRESH_TOKEN_NOT_FOUND
	})
	@SuccessCodeAnnotation(SuccessCode.LOGOUT_SUCCESS)
	@PostMapping("/logout")
	public CommonApiResponse<Void> logout(@Valid @RequestBody TokenRefreshRequest request) {
		authService.logout(request.refreshToken());
		return CommonApiResponse.success(SuccessCode.LOGOUT_SUCCESS);
	}

	@Operation(summary = "OAuth2 토큰 교환", description = "OAuth2 임시 코드를 JWT 토큰으로 교환합니다.")
	@ApiExceptions({
		ErrorCode.TOKEN_INVALID
	})
	@SuccessCodeAnnotation(SuccessCode.OAUTH2_LOGIN_SUCCESS)
	@GetMapping("/oauth2/token")
	public CommonApiResponse<LoginResponse> exchangeOAuth2Token(@RequestParam String code) {
		LoginResponse response = oauth2AuthService.exchangeOAuth2Code(code);
		return CommonApiResponse.success(SuccessCode.OAUTH2_LOGIN_SUCCESS, response);
	}
}
