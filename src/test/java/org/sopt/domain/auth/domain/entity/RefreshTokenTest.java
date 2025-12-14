package org.sopt.domain.auth.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

	@Test
	@DisplayName("RefreshToken 생성 - create 팩토리 메서드")
	void create_Success() {
		// given
		Long memberId = 1L;
		String token = "test-refresh-token";
		LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

		// when
		RefreshToken refreshToken = RefreshToken.create(memberId, token, expiryDate);

		// then
		assertThat(refreshToken.getMemberId()).isEqualTo(memberId);
		assertThat(refreshToken.getToken()).isEqualTo(token);
		assertThat(refreshToken.getExpiryDate()).isEqualTo(expiryDate);
		assertThat(refreshToken.isBlacklisted()).isFalse();
	}

	@Test
	@DisplayName("생성 시 isBlacklisted 기본값은 false")
	void create_DefaultIsBlacklistedIsFalse() {
		// given & when
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"token",
			LocalDateTime.now().plusDays(1)
		);

		// then
		assertThat(refreshToken.isBlacklisted()).isFalse();
	}

	@Test
	@DisplayName("블랙리스트 처리 - markAsBlacklisted")
	void markAsBlacklisted_Success() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"token-to-blacklist",
			LocalDateTime.now().plusDays(1)
		);

		assertThat(refreshToken.isBlacklisted()).isFalse();

		// when
		refreshToken.markAsBlacklisted();

		// then
		assertThat(refreshToken.isBlacklisted()).isTrue();
	}

	@Test
	@DisplayName("블랙리스트 처리 후 다시 호출해도 true 유지")
	void markAsBlacklisted_IdempotentOperation() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"token",
			LocalDateTime.now().plusDays(1)
		);

		// when
		refreshToken.markAsBlacklisted();
		refreshToken.markAsBlacklisted(); // 두 번 호출

		// then
		assertThat(refreshToken.isBlacklisted()).isTrue();
	}

	@Test
	@DisplayName("만료 시간이 과거인 토큰 생성 가능")
	void create_WithPastExpiryDate() {
		// given
		LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

		// when
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"expired-token",
			pastDate
		);

		// then
		assertThat(refreshToken.getExpiryDate()).isEqualTo(pastDate);
		assertThat(refreshToken.getExpiryDate()).isBefore(LocalDateTime.now());
	}

	@Test
	@DisplayName("만료 시간이 미래인 토큰 생성")
	void create_WithFutureExpiryDate() {
		// given
		LocalDateTime futureDate = LocalDateTime.now().plusDays(30);

		// when
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"valid-token",
			futureDate
		);

		// then
		assertThat(refreshToken.getExpiryDate()).isEqualTo(futureDate);
		assertThat(refreshToken.getExpiryDate()).isAfter(LocalDateTime.now());
	}

	@Test
	@DisplayName("동일한 memberId로 여러 토큰 생성 가능 (객체 레벨)")
	void create_MultipleTokensForSameMember() {
		// given
		Long memberId = 100L;

		// when
		RefreshToken token1 = RefreshToken.create(memberId, "token-1", LocalDateTime.now().plusDays(1));
		RefreshToken token2 = RefreshToken.create(memberId, "token-2", LocalDateTime.now().plusDays(1));

		// then
		assertThat(token1.getMemberId()).isEqualTo(memberId);
		assertThat(token2.getMemberId()).isEqualTo(memberId);
		assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
	}

	@Test
	@DisplayName("토큰 문자열이 서로 다른 여러 토큰 생성")
	void create_DifferentTokenStrings() {
		// given & when
		RefreshToken token1 = RefreshToken.create(1L, "unique-token-1", LocalDateTime.now().plusDays(1));
		RefreshToken token2 = RefreshToken.create(2L, "unique-token-2", LocalDateTime.now().plusDays(1));
		RefreshToken token3 = RefreshToken.create(3L, "unique-token-3", LocalDateTime.now().plusDays(1));

		// then
		assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
		assertThat(token2.getToken()).isNotEqualTo(token3.getToken());
		assertThat(token1.getToken()).isNotEqualTo(token3.getToken());
	}
}
