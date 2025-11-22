package org.sopt.domain.member.presentation.controller;

import java.time.Duration;

import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.service.AuthService;
import org.sopt.global.response.CommonApiResponse;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "헤더 기반 Basic-Authentication")
	@PostMapping("/v1/login")
	public CommonApiResponse<MemberResponse> login(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		log.info(authorization);
		MemberResponse result = authService.login(authorization);
		return CommonApiResponse.success(SuccessCode.SUCCESS,result);
	}

	@Operation(summary = "이메일/비밀번호 기반 로그인 (쿠키 발급)")
	@PostMapping("/v2/login")
	public ResponseEntity<CommonApiResponse<MemberResponse>> loginV2(
		@RequestParam("email") String email,
		@RequestParam("password") String password
	) {
		MemberResponse result = authService.loginWithCredentials(email, password);

		String credentials = email + ":" + password;

		ResponseCookie cookie = ResponseCookie.from("basic", credentials)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(Duration.ofHours(1))
			.path("/")
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(CommonApiResponse.success(SuccessCode.SUCCESS, result));
	}
}


