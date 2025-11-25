package org.sopt.global.auth.oauth2.strategy;

import java.util.Map;
import org.sopt.global.auth.oauth2.dto.KakaoUserInfo;
import org.sopt.global.auth.oauth2.dto.OAuth2UserInfo;
import org.springframework.stereotype.Component;

@Component
public class KakaoUserInfoStrategy implements OAuth2UserInfoStrategy {

	@Override
	public OAuth2UserInfo extractUserInfo(Map<String, Object> attributes) {
		return new KakaoUserInfo(attributes);
	}
}
