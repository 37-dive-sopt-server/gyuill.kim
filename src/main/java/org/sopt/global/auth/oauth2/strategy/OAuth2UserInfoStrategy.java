package org.sopt.global.auth.oauth2.strategy;

import java.util.Map;

import org.sopt.global.auth.oauth2.dto.OAuth2UserInfo;

public interface OAuth2UserInfoStrategy {
	OAuth2UserInfo extractUserInfo(Map<String, Object> attributes);
}
