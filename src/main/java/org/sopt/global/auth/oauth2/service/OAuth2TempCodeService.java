package org.sopt.global.auth.oauth2.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.sopt.domain.auth.application.dto.TokenPair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OAuth2TempCodeService {

	private static final long EXPIRATION_SECONDS = 30;
	private final Map<String, TempCodeData> tempCodes = new ConcurrentHashMap<>();

	public String generateCode(TokenPair tokens) {
		String code = UUID.randomUUID().toString();
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS);
		tempCodes.put(code, new TempCodeData(tokens, expiresAt));
		log.debug("Temporary code generated: {}", code);
		return code;
	}

	public TokenPair consumeCode(String code) {
		TempCodeData data = tempCodes.remove(code);

		if (data == null) {
			log.warn("Invalid or already used code: {}", code);
			return null;
		}

		if (data.expiresAt.isBefore(LocalDateTime.now())) {
			log.warn("Expired code: {}", code);
			return null;
		}

		log.debug("Code consumed successfully: {}", code);
		return data.tokens;
	}

	@Scheduled(fixedRate = 60000)
	public void cleanupExpiredCodes() {
		LocalDateTime now = LocalDateTime.now();
		tempCodes.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
		log.debug("Cleaned up expired temporary codes. Remaining: {}", tempCodes.size());
	}

	private record TempCodeData(TokenPair tokens, LocalDateTime expiresAt) {
	}
}
