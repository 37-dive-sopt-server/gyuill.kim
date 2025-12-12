package org.sopt.global.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("articles");

		cacheManager.setCaffeine(Caffeine.newBuilder()
			.maximumSize(10_000)  // 최대 10,000개 게시글 캐싱
			.expireAfterWrite(Duration.ofMinutes(5))  // 5분 후 만료
			.recordStats());  // 통계 수집 활성화

		return cacheManager;
	}
}
