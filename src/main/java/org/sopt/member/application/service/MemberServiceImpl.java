package org.sopt.member.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.sopt.member.domain.entity.Member;
import org.sopt.member.application.dto.MemberCreateRequest;
import org.sopt.member.application.dto.MemberResponse;
import org.sopt.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	public MemberServiceImpl(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public MemberResponse join(MemberCreateRequest request) {

		if (memberRepository.findByEmail(request.email()).isPresent()) {
        	throw new IllegalArgumentException("이미 존재하는 이메일입니다");
		}

		Long id = memberRepository.generateNextId();
		Member member = new Member(
			id,
			request.name(),
			request.birthDate(),
			request.email(),
			request.gender()
		);
		memberRepository.save(member);
		return MemberResponse.from(member);
	}

	@Override
	public MemberResponse findMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + memberId));
		return MemberResponse.from(member);
	}

	@Override
	public List<MemberResponse> findAllMembers() {
		return memberRepository.findAll().stream()
			.map(MemberResponse::from)
			.collect(Collectors.toList());
	}

	@Override
	public void deleteMember(String email) {
		boolean deleted = memberRepository.deleteByEmail(email);
		if (!deleted) {
			throw new IllegalArgumentException("회원을 찾을 수 없습니다. Email: " + email);
		}
	}
}
