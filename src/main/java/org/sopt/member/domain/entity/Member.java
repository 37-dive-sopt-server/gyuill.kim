package org.sopt.member.domain.entity;

import java.time.LocalDate;

public record Member(Long id, String name, LocalDate birthDate, String email, Gender gender) {

	public Member {
		validateName(name);
		validateBirthDate(birthDate);
		validateEmail(email);
		validateGender(gender);
	}

	private void validateName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("이름을 입력해주세요");
		}
	}

	private void validateBirthDate(LocalDate birthDate) {
		if (birthDate == null) {
			throw new IllegalArgumentException("생년월일을 입력해주세요");
		}
		if (birthDate.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("생년월일은 과거 날짜여야 합니다");
		}
		int age = LocalDate.now().getYear() - birthDate.getYear();
		if (age < 20) {
			throw new IllegalArgumentException("20세 미만은 회원 가입이 불가능합니다");
		}
	}

	private void validateEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("이메일을 입력해주세요");
		}
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.matches(emailRegex)) {
			throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다");
		}
	}

	private void validateGender(Gender gender) {
		if (gender == null) {
			throw new IllegalArgumentException("성별을 선택해주세요");
		}
	}
}