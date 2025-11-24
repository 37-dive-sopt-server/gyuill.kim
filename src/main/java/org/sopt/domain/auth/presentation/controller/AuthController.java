package org.sopt.domain.auth.presentation.controller;

import org.sopt.domain.auth.application.dto.LoginRequest;
import org.sopt.domain.auth.application.dto.LoginResponse;
import org.sopt.domain.auth.application.dto.TokenRefreshRequest;
import org.sopt.domain.auth.application.dto.TokenRefreshResponse;
import org.sopt.domain.auth.application.service.AuthService;
import org.sopt.global.annotation.ApiExceptions;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.CommonApiResponse;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
