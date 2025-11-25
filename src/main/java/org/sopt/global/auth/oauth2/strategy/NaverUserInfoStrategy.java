package org.sopt.global.auth.oauth2.strategy;

import java.util.Map;
import org.sopt.global.auth.oauth2.dto.NaverUserInfo;
import org.sopt.global.auth.oauth2.dto.OAuth2UserInfo;
import org.springframework.stereotype.Component;

@Component
public class NaverUserInfoStrategy implements OAuth2UserInfoStrategy {

	@Override
	public OAuth2UserInfo extractUserInfo(Map<String, Object> attributes) {
		return new NaverUserInfo(attributes);
	}
}
