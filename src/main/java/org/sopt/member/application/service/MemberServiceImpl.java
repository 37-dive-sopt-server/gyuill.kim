package org.sopt.member.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.sopt.global.exception.BaseException;
import org.sopt.global.response.error.ErrorCode;
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
        	throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
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
			.orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
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
			throw new BaseException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}
}
