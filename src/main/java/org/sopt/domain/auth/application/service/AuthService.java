package org.sopt.domain.auth.application.service;

import java.time.LocalDateTime;

import org.sopt.domain.auth.application.dto.TokenPair;
import org.sopt.domain.auth.application.dto.request.LoginRequest;
import org.sopt.domain.auth.application.dto.request.TokenRefreshRequest;
import org.sopt.domain.auth.application.dto.response.LoginResponse;
import org.sopt.domain.auth.application.dto.response.TokenRefreshResponse;
import org.sopt.domain.auth.domain.entity.RefreshToken;
import org.sopt.domain.auth.domain.repository.RefreshTokenRepository;
import org.sopt.domain.auth.exception.AuthException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.global.auth.jwt.JwtProperties;
import org.sopt.global.auth.jwt.JwtProvider;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new AuthException(ErrorCode.LOGIN_FAIL));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new AuthException(ErrorCode.LOGIN_FAIL);
		}

		TokenPair tokens = generateAndSaveTokens(member.getId());

		return LoginResponse.of(tokens.accessToken(), tokens.refreshToken(), jwtProperties.getExpiresInSeconds());
	}

	@Transactional
	public TokenRefreshResponse renewalRefreshToken(TokenRefreshRequest request) {
		String requestToken = request.refreshToken();

		validateRefreshToken(requestToken);

		RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
			.orElseThrow(() -> new AuthException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

		Long memberId = jwtProvider.getUserIdFromToken(requestToken);
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new AuthException(ErrorCode.MEMBER_NOT_FOUND));

		refreshToken.markAsBlacklisted();

		TokenPair tokens = generateAndSaveTokens(member.getId());

		return TokenRefreshResponse.of(tokens.accessToken(), tokens.refreshToken(),
			jwtProperties.getExpiresInSeconds());
	}

	@Transactional
	public void logout(String refreshToken) {
		RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
			.orElseThrow(() -> new AuthException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

		token.markAsBlacklisted();
	}

	private void validateRefreshToken(String token) {
		// 1. JWT 만료 여부 체크
		if (jwtProvider.isTokenExpired(token)) {
			throw new AuthException(ErrorCode.TOKEN_EXPIRED);
		}

		// 2. JWT 서명 검증
		if (!jwtProvider.validateToken(token)) {
			throw new AuthException(ErrorCode.TOKEN_INVALID);
		}

		// 3. DB에서 Refresh Token 조회 및 상태 확인
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
			.orElseThrow(() -> new AuthException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

		// 4. 블랙리스트 여부 체크
		if (refreshToken.isBlacklisted()) {
			throw new AuthException(ErrorCode.TOKEN_BLACKLISTED);
		}
	}

	@Transactional
	public TokenPair generateAndSaveTokens(Long userId) {
		String accessToken = jwtProvider.generateAccessToken(userId);
		String refreshToken = jwtProvider.generateRefreshToken(userId);

		LocalDateTime expiryDate = LocalDateTime.now()
			.plusSeconds(jwtProperties.getRefreshExpiresInSeconds());

		refreshTokenRepository.findByMemberId(userId)
			.ifPresent(refreshTokenRepository::delete);

		RefreshToken refreshTokenEntity = RefreshToken.create(userId, refreshToken, expiryDate);
		refreshTokenRepository.save(refreshTokenEntity);

		return new TokenPair(accessToken, refreshToken);
	}
}
