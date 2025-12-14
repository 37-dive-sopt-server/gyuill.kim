package org.sopt.domain.auth.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.annotation.RepositoryTest;
import org.sopt.domain.auth.domain.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@RepositoryTest
class RefreshTokenRepositoryTest {

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("토큰 문자열로 RefreshToken 조회 성공")
	void findByToken_Success() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			"test-refresh-token",
			LocalDateTime.now().plusDays(1)
		);
		refreshTokenRepository.save(refreshToken);
		entityManager.clear();

		// when
		Optional<RefreshToken> result = refreshTokenRepository.findByToken("test-refresh-token");

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getToken()).isEqualTo("test-refresh-token");
		assertThat(result.get().getMemberId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("존재하지 않는 토큰으로 조회 시 빈 Optional 반환")
	void findByToken_NotFound() {
		// when
		Optional<RefreshToken> result = refreshTokenRepository.findByToken("nonexistent-token");

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("회원 ID로 RefreshToken 조회 성공")
	void findByMemberId_Success() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			100L,
			"member-100-token",
			LocalDateTime.now().plusDays(1)
		);
		refreshTokenRepository.save(refreshToken);
		entityManager.clear();

		// when
		Optional<RefreshToken> result = refreshTokenRepository.findByMemberId(100L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getMemberId()).isEqualTo(100L);
		assertThat(result.get().getToken()).isEqualTo("member-100-token");
	}

	@Test
	@DisplayName("존재하지 않는 회원 ID로 조회 시 빈 Optional 반환")
	void findByMemberId_NotFound() {
		// when
		Optional<RefreshToken> result = refreshTokenRepository.findByMemberId(999L);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("만료 시간 이전의 토큰 삭제 - deleteByExpiryDateBefore")
	void deleteByExpiryDateBefore() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// 만료된 토큰 2개
		RefreshToken expiredToken1 = RefreshToken.create(1L, "expired-token-1", now.minusDays(2));
		RefreshToken expiredToken2 = RefreshToken.create(2L, "expired-token-2", now.minusHours(1));

		// 유효한 토큰 2개
		RefreshToken validToken1 = RefreshToken.create(3L, "valid-token-1", now.plusDays(1));
		RefreshToken validToken2 = RefreshToken.create(4L, "valid-token-2", now.plusDays(7));

		refreshTokenRepository.save(expiredToken1);
		refreshTokenRepository.save(expiredToken2);
		refreshTokenRepository.save(validToken1);
		refreshTokenRepository.save(validToken2);

		Long expiredId1 = expiredToken1.getId();
		Long expiredId2 = expiredToken2.getId();
		Long validId1 = validToken1.getId();
		Long validId2 = validToken2.getId();

		entityManager.clear();

		// when - 현재 시간 이전에 만료된 토큰 삭제
		refreshTokenRepository.deleteByExpiryDateBefore(now);

		// then
		assertThat(refreshTokenRepository.findById(expiredId1)).isEmpty();
		assertThat(refreshTokenRepository.findById(expiredId2)).isEmpty();
		assertThat(refreshTokenRepository.findById(validId1)).isPresent();
		assertThat(refreshTokenRepository.findById(validId2)).isPresent();
	}

	@Test
	@DisplayName("RefreshToken 저장 및 ID 자동 생성 확인")
	void save_GeneratesId() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			10L,
			"new-refresh-token",
			LocalDateTime.now().plusDays(7)
		);

		// when
		RefreshToken saved = refreshTokenRepository.save(refreshToken);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getToken()).isEqualTo("new-refresh-token");
		assertThat(saved.getMemberId()).isEqualTo(10L);
		assertThat(saved.isBlacklisted()).isFalse();
	}

	@Test
	@DisplayName("RefreshToken 저장 시 createdAt 자동 설정 확인")
	void save_SetsCreatedAt() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			20L,
			"token-with-created-at",
			LocalDateTime.now().plusDays(1)
		);

		// when
		RefreshToken saved = refreshTokenRepository.save(refreshToken);

		// then
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
	}

	@Test
	@DisplayName("블랙리스트 처리 - markAsBlacklisted")
	void markAsBlacklisted() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			30L,
			"blacklist-token",
			LocalDateTime.now().plusDays(1)
		);
		RefreshToken saved = refreshTokenRepository.save(refreshToken);
		entityManager.clear();

		// when
		RefreshToken found = refreshTokenRepository.findById(saved.getId()).get();
		found.markAsBlacklisted();
		entityManager.flush(); // dirty checking으로 변경사항 DB 반영
		entityManager.clear();

		// then
		RefreshToken updated = refreshTokenRepository.findById(saved.getId()).get();
		assertThat(updated.isBlacklisted()).isTrue();
	}

	@Test
	@DisplayName("토큰 unique constraint 위반 시 예외 발생")
	void saveWithDuplicateToken_ThrowsException() {
		// given
		RefreshToken token1 = RefreshToken.create(1L, "duplicate-token", LocalDateTime.now().plusDays(1));
		refreshTokenRepository.save(token1);
		entityManager.flush();
		entityManager.clear();

		RefreshToken token2 = RefreshToken.create(2L, "duplicate-token", LocalDateTime.now().plusDays(1));

		// when & then
		assertThatThrownBy(() -> {
			refreshTokenRepository.save(token2);
			entityManager.flush();  // flush to trigger constraint check
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("RefreshToken 삭제")
	void delete_Success() {
		// given
		RefreshToken refreshToken = RefreshToken.create(
			40L,
			"delete-token",
			LocalDateTime.now().plusDays(1)
		);
		RefreshToken saved = refreshTokenRepository.save(refreshToken);
		Long tokenId = saved.getId();
		entityManager.clear();

		// when
		refreshTokenRepository.deleteById(tokenId);

		// then
		Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("동일한 회원의 기존 토큰 조회 및 교체 시나리오")
	void replaceExistingToken_Scenario() {
		// given - 기존 토큰 저장
		RefreshToken oldToken = RefreshToken.create(
			50L,
			"old-token",
			LocalDateTime.now().plusDays(1)
		);
		refreshTokenRepository.save(oldToken);
		Long oldTokenId = oldToken.getId();
		entityManager.clear();

		// when - 기존 토큰 조회 및 삭제
		Optional<RefreshToken> found = refreshTokenRepository.findByMemberId(50L);
		assertThat(found).isPresent();

		refreshTokenRepository.delete(found.get());

		// 새 토큰 저장
		RefreshToken newToken = RefreshToken.create(
			50L,
			"new-token",
			LocalDateTime.now().plusDays(7)
		);
		refreshTokenRepository.save(newToken);
		entityManager.flush(); // 모든 변경사항 DB 반영
		entityManager.clear();

		// then - 기존 토큰은 삭제되고 새 토큰만 존재
		assertThat(refreshTokenRepository.findById(oldTokenId)).isEmpty();
		Optional<RefreshToken> currentToken = refreshTokenRepository.findByMemberId(50L);
		assertThat(currentToken).isPresent();
		assertThat(currentToken.get().getToken()).isEqualTo("new-token");
	}
}
