package org.sopt.global.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CacheMonitor {

	private final CacheManager cacheManager;

	@Scheduled(fixedRate = 60000)  // 1분마다 실행
	public void logCacheStats() {
		cacheManager.getCacheNames().forEach(cacheName -> {
			CaffeineCache caffeineCache = (CaffeineCache)cacheManager.getCache(cacheName);
			if (caffeineCache != null) {
				com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
					caffeineCache.getNativeCache();

				CacheStats stats = nativeCache.stats();

				log.info("Cache '{}' statistics - " +
						"Hit Rate: {:.2f}%, " +
						"Hits: {}, " +
						"Misses: {}, " +
						"Evictions: {}, " +
						"Size: {}",
					cacheName,
					stats.hitRate() * 100,
					stats.hitCount(),
					stats.missCount(),
					stats.evictionCount(),
					nativeCache.estimatedSize()
				);
			}
		});
	}
}
