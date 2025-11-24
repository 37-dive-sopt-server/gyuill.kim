package org.sopt.domain.auth.domain.repository;

import org.sopt.domain.auth.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    boolean existsByTokenAndIsBlacklistedFalse(String token);
}
