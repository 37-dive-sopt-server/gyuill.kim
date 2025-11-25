package org.sopt.global.auth.oauth2.handler;

import java.io.IOException;

import org.sopt.domain.auth.application.dto.TokenPair;
import org.sopt.domain.auth.application.service.AuthService;
import org.sopt.global.auth.jwt.JwtProperties;
import org.sopt.global.auth.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthService authService;
	private final JwtProperties jwtProperties;

	@Value("${oauth2.success-redirect-url:http://localhost:3000/oauth2/redirect}")
	private String redirectUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		Long userId = userDetails.getMemberId();

		TokenPair tokens = authService.generateAndSaveTokens(userId);

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
			.queryParam("accessToken", tokens.accessToken())
			.queryParam("refreshToken", tokens.refreshToken())
			.queryParam("expiresIn", jwtProperties.getExpiresInSeconds())
			.build()
			.toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
