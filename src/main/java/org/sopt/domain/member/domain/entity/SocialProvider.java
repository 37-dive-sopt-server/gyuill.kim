package org.sopt.domain.member.domain.entity;

import lombok.Getter;

@Getter
public enum SocialProvider {
	LOCAL("일반 회원가입"),
	GOOGLE("구글"),
	KAKAO("카카오"),
	NAVER("네이버");

	private final String description;

	SocialProvider(String description) {
		this.description = description;
	}
}
