package org.sopt.global.auth.oauth2.strategy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OAuth2UserInfoStrategyFactory {
	private final Map<String, OAuth2UserInfoStrategy> strategies;

	public OAuth2UserInfoStrategyFactory(
		GoogleUserInfoStrategy googleStrategy,
		KakaoUserInfoStrategy kakaoStrategy,
		NaverUserInfoStrategy naverStrategy
	) {
		this.strategies = new HashMap<>();
		strategies.put("google", googleStrategy);
		strategies.put("kakao", kakaoStrategy);
		strategies.put("naver", naverStrategy);
	}

	public OAuth2UserInfoStrategy getStrategy(String registrationId) {
		OAuth2UserInfoStrategy strategy = strategies.get(registrationId.toLowerCase());
		if (strategy == null) {
			throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
		}
		return strategy;
	}
}
