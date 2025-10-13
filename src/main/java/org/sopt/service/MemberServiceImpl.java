package org.sopt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.sopt.domain.Gender;
import org.sopt.domain.Member;
import org.sopt.repository.MemoryMemberRepository;

public class MemberServiceImpl implements  MemberService {

	private final MemoryMemberRepository memberRepository = new MemoryMemberRepository();
	private static long sequence = 1L;

	public Long join(String name, LocalDate birthDate, String email, Gender gender) {
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new IllegalStateException("이미 존재하는 이메일입니다.");
		}

		int age = LocalDate.now().getYear() - birthDate.getYear();

		if (age < 20) {
			throw new IllegalStateException("20세 미만은 회원 가입이 불가능합니다.");
		}

		Member member = new Member(sequence++, name, birthDate, email, gender);
		memberRepository.save(member);
		return member.getId();
	}

	public Optional<Member> findOne(Long memberId) {
		return memberRepository.findById(memberId);
	}

	public List<Member> findAllMembers() {
		return memberRepository.findAll();
	}

	public boolean deleteMember(String email) {
		return memberRepository.deleteByEmail(email);
	}
}
