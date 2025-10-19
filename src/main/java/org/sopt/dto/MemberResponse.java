package org.sopt.dto;

import java.time.LocalDate;

import org.sopt.domain.Gender;
import org.sopt.domain.Member;

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
