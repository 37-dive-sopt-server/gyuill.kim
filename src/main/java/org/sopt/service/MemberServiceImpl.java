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
}
