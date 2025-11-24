package org.sopt.domain.member.application.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
public class JwtService {

	private final Algorithm algorithm;
	private final long defaultExpiresInSeconds;

	public JwtService(
		@Value("${security.jwt.secret}") String secret,
		@Value("${security.jwt.expires-in-seconds:3600}") long defaultExpiresInSeconds
	) {
		this.algorithm = Algorithm.HMAC256(secret);
		this.defaultExpiresInSeconds = defaultExpiresInSeconds;
	}

	public String generateToken(Long memberId, String email) {
		return generateToken(memberId, email, defaultExpiresInSeconds);
	}

	public String generateToken(Long memberId, String email, long expiresInSeconds) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(expiresInSeconds);

		return JWT.create()
			.withSubject(String.valueOf(memberId))
			.withClaim("email", email)
			.withIssuedAt(Date.from(now))
			.withExpiresAt(Date.from(exp))
			.sign(algorithm);
	}

	public Long verifyAndGetMemberId(String token) {
		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("토큰이 없습니다.");
		}
		DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
		String sub = jwt.getSubject();
		try {
			return Long.parseLong(sub);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("JWT의 회원 정보가 올바르지 않습니다.");
		}
	}
}
