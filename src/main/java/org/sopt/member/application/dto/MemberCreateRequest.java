package org.sopt.member.application.dto;

import java.time.LocalDate;

import org.sopt.member.domain.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 가입 요청")
public record MemberCreateRequest(
	@Schema(description = "회원 이름", example = "김솝트", requiredMode = Schema.RequiredMode.REQUIRED)
	String name,

	@Schema(description = "생년월일 (yyyy-MM-dd)", example = "2000-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
	LocalDate birthDate,

	@Schema(description = "이메일 주소", example = "sopt@sopt.org", requiredMode = Schema.RequiredMode.REQUIRED)
	String email,

	@Schema(description = "성별 (MALE/FEMALE)", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED)
	Gender gender
) {
}
