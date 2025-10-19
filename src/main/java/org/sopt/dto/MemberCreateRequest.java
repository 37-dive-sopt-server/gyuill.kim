package org.sopt.dto;

import java.time.LocalDate;

import org.sopt.domain.Gender;

public record MemberCreateRequest(
	String name,
	LocalDate birthDate,
	String email,
	Gender gender
) {
}
