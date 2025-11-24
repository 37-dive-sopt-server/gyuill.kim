package org.sopt.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.domain.auth.domain.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("만료된 Refresh Token 정리 작업 시작: {}", now);

        try {
            refreshTokenRepository.deleteByExpiryDateBefore(now);
            log.info("만료된 Refresh Token 정리 작업 완료: {}", now);
        } catch (Exception e) {
            log.error("만료된 Refresh Token 정리 작업 실패: {}", e.getMessage(), e);
        }
    }
}
