package org.sopt.domain.auth.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.sopt.domain.auth.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	Optional<RefreshToken> findByMemberId(Long memberId);

	void deleteByExpiryDateBefore(LocalDateTime dateTime);

}
