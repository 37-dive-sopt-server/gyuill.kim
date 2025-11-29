package org.sopt.domain.member.application.dto;

import org.sopt.domain.member.domain.entity.SocialProvider;
import org.sopt.global.auth.oauth2.dto.OAuth2UserInfo;

public record SocialMemberCreateRequest(
	String email,
	String name,
	SocialProvider provider,
	String providerId,
	String profileImageUrl
) {
	public static SocialMemberCreateRequest from(OAuth2UserInfo userInfo, SocialProvider provider) {
		return new SocialMemberCreateRequest(
			userInfo.getEmail(),
			userInfo.getName(),
			provider,
			userInfo.getProviderId(),
			userInfo.getProfileImageUrl()
		);
	}
}
