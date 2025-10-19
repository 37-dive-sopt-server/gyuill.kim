package org.sopt.member.application.dto;

import java.time.LocalDate;

import org.sopt.member.domain.entity.Gender;
import org.sopt.member.domain.entity.Member;

public record MemberResponse(
	Long id,
	String name,
	LocalDate birthDate,
	String email,
	Gender gender
) {
	public static MemberResponse from(Member member) {
		return new MemberResponse(
			member.id(),
			member.name(),
			member.birthDate(),
			member.email(),
			member.gender()
		);
	}
}
