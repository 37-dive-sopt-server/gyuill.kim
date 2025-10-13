package org.sopt.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.sopt.domain.Gender;
import org.sopt.repository.MemoryMemberRepository;

public class MemberValidator {
	private final MemoryMemberRepository memberRepository;

	public MemberValidator(MemoryMemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public void validateName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("이름을 입력해주세요.");
		}
	}

	public LocalDate validateAndParseBirthDate(String birthDateStr) {
		try {
			LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
			validateAge(birthDate);
			return birthDate;
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("유효하지 않은 날짜 형식입니다. YYYY-MM-DD 형식으로 입력해주세요.");
		}
	}

	public void validateAge(LocalDate birthDate) {
		int age = LocalDate.now().getYear() - birthDate.getYear();
		if (age < 20) {
			throw new IllegalArgumentException("20세 미만은 회원 가입이 불가능합니다.");
		}
	}

	public void validateEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("이메일을 입력해주세요.");
		}
		validateEmailDuplicate(email);
	}

	public void validateEmailDuplicate(String email) {
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
	}

	public Gender validateAndParseGender(String genderChoice) {
		return switch (genderChoice) {
			case "1" -> Gender.MALE;
			case "2" -> Gender.FEMALE;
			case "3" -> Gender.OTHER;
			default -> throw new IllegalArgumentException("유효하지 않은 성별 선택입니다.");
		};
	}
}
