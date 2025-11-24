package org.sopt.domain.auth.application.service;

import java.time.LocalDateTime;

import org.sopt.domain.auth.application.dto.LoginRequest;
import org.sopt.domain.auth.application.dto.LoginResponse;
import org.sopt.domain.auth.application.dto.TokenRefreshRequest;
import org.sopt.domain.auth.application.dto.TokenRefreshResponse;
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

        String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());

        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiresInSeconds());

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshTokenEntity = RefreshToken.create(
                member.getId(),
                refreshToken,
                expiryDate
        );
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponse.of(accessToken, refreshToken, jwtProperties.getExpiresInSeconds());
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

        String newAccessToken = jwtProvider.generateAccessToken(member.getId(), member.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken(member.getId());

        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiresInSeconds());

        RefreshToken newRefreshTokenEntity = RefreshToken.create(
                member.getId(),
                newRefreshToken,
                expiryDate
        );
        refreshTokenRepository.save(newRefreshTokenEntity);

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken, jwtProperties.getExpiresInSeconds());
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        token.markAsBlacklisted();
    }

    public void validateRefreshToken(String token) {
        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(ErrorCode.TOKEN_INVALID);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.isValid()) {
            if (refreshToken.isExpired()) {
                throw new AuthException(ErrorCode.TOKEN_EXPIRED);
            }
            if (refreshToken.isBlacklisted()) {
                throw new AuthException(ErrorCode.TOKEN_BLACKLISTED);
            }
        }
    }
}
