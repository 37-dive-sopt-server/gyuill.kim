package org.sopt.domain.member.application.dto;

import java.time.LocalDate;

import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 응답")
public record MemberResponse(
	@Schema(description = "회원 ID", example = "1")
	Long id,

	@Schema(description = "회원 이름", example = "김솝트")
	String name,

	@Schema(description = "생년월일", example = "2000-01-01")
	LocalDate birthDate,

	@Schema(description = "이메일 주소", example = "sopt@sopt.org")
	String email,

	@Schema(description = "성별", example = "MALE")
	Gender gender
) {
	public static MemberResponse fromEntity(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getName(),
			member.getBirthDate(),
			member.getEmail(),
			member.getGender()
		);
	}
}
