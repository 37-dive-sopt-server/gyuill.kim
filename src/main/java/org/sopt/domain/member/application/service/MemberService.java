package org.sopt.domain.member.application.service;

import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.dto.SocialMemberCreateRequest;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.domain.service.MemberValidator;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberValidator memberValidator;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public MemberResponse create(MemberCreateRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		Member member = memberValidator.createValidatedMember(encodedPassword, request.name(), request.birthDate(),
			request.email(), request.gender());

		memberRepository.save(member);

		return MemberResponse.fromEntity(member);
	}

	public MemberResponse getMemberById(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
		return MemberResponse.fromEntity(member);
	}

	public Page<MemberResponse> findAllMembers(Pageable pageable) {
		return memberRepository.findAll(pageable).map(MemberResponse::fromEntity);
	}

	@Transactional
	public void deleteMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
		memberRepository.delete(member);
	}

	@Transactional
	public Member getOrCreateSocialMember(SocialMemberCreateRequest request) {
		return memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.orElseGet(() -> {
				// 같은 이메일이 이미 존재하면 중복 에러
				if (memberRepository.existsByEmail(request.email())) {
					throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
				}

				Member newMember = Member.createSocialMember(
					request.email(),
					request.name(),
					request.provider(),
					request.providerId(),
					request.profileImageUrl()
				);
				return memberRepository.save(newMember);
			});
	}
}
