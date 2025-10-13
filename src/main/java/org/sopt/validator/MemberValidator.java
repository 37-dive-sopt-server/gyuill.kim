package org.sopt.validator;

import java.time.LocalDate;

import org.sopt.repository.MemoryMemberRepository;

public class MemberValidator {
	private final MemoryMemberRepository memberRepository;

	public MemberValidator(MemoryMemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public void validateAge(LocalDate birthDate) {
		int age = LocalDate.now().getYear() - birthDate.getYear();
		if (age < 20) {
			throw new IllegalArgumentException("20세 미만은 회원 가입이 불가능합니다.");
		}
	}

	public void validateEmailDuplicate(String email) {
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
	}
}
