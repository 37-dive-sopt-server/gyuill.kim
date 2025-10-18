package org.sopt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.sopt.domain.Gender;
import org.sopt.domain.Member;
import org.sopt.repository.MemberRepository;

public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	public MemberServiceImpl(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public Long join(String name, LocalDate birthDate, String email, Gender gender) {
		Long id = memberRepository.generateNextId();
		Member member = new Member(id, name, birthDate, email, gender);
		memberRepository.save(member);
		return member.id();
	}

	@Override
	public Optional<Member> findOne(Long memberId) {
		return memberRepository.findById(memberId);
	}

	@Override
	public List<Member> findAllMembers() {
		return memberRepository.findAll();
	}

	@Override
	public boolean deleteMember(String email) {
		return memberRepository.deleteByEmail(email);
	}
}
