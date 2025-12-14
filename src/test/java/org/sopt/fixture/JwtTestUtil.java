package org.sopt.fixture;

import java.time.Instant;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class JwtTestUtil {

	private static final String TEST_SECRET = "test-jwt-secret-key-minimum-256-bits-required-for-hs256-algorithm-security-testing";
	private static final long ACCESS_TOKEN_VALIDITY = 3600; // 1 hour
	private static final long REFRESH_TOKEN_VALIDITY = 86400; // 24 hours

	public static String generateValidAccessToken(Long userId) {
		Instant now = Instant.now();
		Instant expiryDate = now.plusSeconds(ACCESS_TOKEN_VALIDITY);

		return JWT.create()
			.withSubject(String.valueOf(userId))
			.withIssuedAt(Date.from(now))
			.withExpiresAt(Date.from(expiryDate))
			.sign(Algorithm.HMAC256(TEST_SECRET));
	}

	public static String generateValidRefreshToken(Long userId) {
		Instant now = Instant.now();
		Instant expiryDate = now.plusSeconds(REFRESH_TOKEN_VALIDITY);

		return JWT.create()
			.withSubject(String.valueOf(userId))
			.withIssuedAt(Date.from(now))
			.withExpiresAt(Date.from(expiryDate))
			.sign(Algorithm.HMAC256(TEST_SECRET));
	}

	public static String generateExpiredToken(Long userId) {
		Instant pastTime = Instant.now().minusSeconds(7200); // 2 hours ago
		Instant expiryTime = pastTime.plusSeconds(3600); // Expired 1 hour ago

		return JWT.create()
			.withSubject(String.valueOf(userId))
			.withIssuedAt(Date.from(pastTime))
			.withExpiresAt(Date.from(expiryTime))
			.sign(Algorithm.HMAC256(TEST_SECRET));
	}

	public static String generateInvalidSignatureToken(Long userId) {
		Instant now = Instant.now();
		Instant expiryDate = now.plusSeconds(ACCESS_TOKEN_VALIDITY);

		return JWT.create()
			.withSubject(String.valueOf(userId))
			.withIssuedAt(Date.from(now))
			.withExpiresAt(Date.from(expiryDate))
			.sign(Algorithm.HMAC256("wrong-secret-key-that-does-not-match"));
	}

	public static String generateTokenWithCustomExpiry(Long userId, long expirySeconds) {
		Instant now = Instant.now();
		Instant expiryDate = now.plusSeconds(expirySeconds);

		return JWT.create()
			.withSubject(String.valueOf(userId))
			.withIssuedAt(Date.from(now))
			.withExpiresAt(Date.from(expiryDate))
			.sign(Algorithm.HMAC256(TEST_SECRET));
	}
}
