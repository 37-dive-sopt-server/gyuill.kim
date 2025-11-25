package org.sopt.global.auth.oauth2.dto;

public interface OAuth2UserInfo {
	String getProviderId();

	String getEmail();

	String getName();

	String getProfileImageUrl();
}
