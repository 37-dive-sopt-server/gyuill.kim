package org.sopt.global.auth.oauth2.dto;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attributes;

	public NaverUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getProviderId() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		if (response == null) {
			return null;
		}
		return (String) response.get("id");
	}

	@Override
	public String getEmail() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		if (response == null) {
			return null;
		}
		return (String) response.get("email");
	}

	@Override
	public String getName() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		if (response == null) {
			return null;
		}
		return (String) response.get("name");
	}

	@Override
	public String getProfileImageUrl() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		if (response == null) {
			return null;
		}
		return (String) response.get("profile_image");
	}
}
