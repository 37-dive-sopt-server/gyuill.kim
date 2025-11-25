package org.sopt.global.auth.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.domain.auth.domain.entity.RefreshToken;
import org.sopt.domain.auth.domain.repository.RefreshTokenRepository;
import org.sopt.global.auth.jwt.JwtProperties;
import org.sopt.global.auth.jwt.JwtProvider;
import org.sopt.global.auth.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${oauth2.success-redirect-url:http://localhost:3000/oauth2/redirect}")
	private String redirectUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Long userId = userDetails.getId();

		String accessToken = jwtProvider.generateAccessToken(userId);
		String refreshToken = jwtProvider.generateRefreshToken(userId);

		LocalDateTime expiryDate = LocalDateTime.now()
			.plusSeconds(jwtProperties.getRefreshExpiresInSeconds());

		refreshTokenRepository.findByMemberId(userId)
			.ifPresent(refreshTokenRepository::delete);

		RefreshToken refreshTokenEntity = RefreshToken.create(userId, refreshToken, expiryDate);
		refreshTokenRepository.save(refreshTokenEntity);

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
			.queryParam("accessToken", accessToken)
			.queryParam("refreshToken", refreshToken)
			.queryParam("expiresIn", jwtProperties.getExpiresInSeconds())
			.build()
			.toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
