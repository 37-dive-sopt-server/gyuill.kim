package org.sopt.member.application.dto;

import java.time.LocalDate;

import org.sopt.member.domain.entity.Gender;

public record MemberCreateRequest(
	String name,
	LocalDate birthDate,
	String email,
	Gender gender
) {
}
