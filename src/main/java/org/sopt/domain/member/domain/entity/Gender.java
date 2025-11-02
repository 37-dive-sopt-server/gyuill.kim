package org.sopt.domain.member.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성별", enumAsRef = true)
public enum Gender {
	@Schema(description = "남성")
	MALE("남성"),

	@Schema(description = "여성")
	FEMALE("여성"),

	@Schema(description = "기타")
	OTHER("기타");

	private final String description;

	Gender(String description) {
		this.description = description;
	}
}
