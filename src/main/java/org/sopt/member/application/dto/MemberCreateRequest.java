package org.sopt.member.application.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import java.time.LocalDate;

import org.sopt.member.domain.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 가입 요청")
public record MemberCreateRequest(
	@Schema(description = "회원 이름", example = "김솝트", requiredMode = REQUIRED)
	String name,

	@Schema(description = "생년월일 (yyyy-MM-dd)", example = "2000-01-01", requiredMode = REQUIRED)
	LocalDate birthDate,

	@Schema(description = "이메일 주소", example = "sopt@sopt.org", requiredMode = REQUIRED)
	String email,

	@Schema(description = "성별 (MALE/FEMALE/OTHER)", example = "MALE", requiredMode = REQUIRED)
	Gender gender
) {
}
