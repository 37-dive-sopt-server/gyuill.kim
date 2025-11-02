package org.sopt.domain.member.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.sopt.global.exception.BaseException;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse join(MemberCreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(
                request.name(),
                request.birthDate(),
                request.email(),
                request.gender()
        );
        memberRepository.save(member);
        return MemberResponse.from(member);
    }

    public MemberResponse findMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    public Page<MemberResponse> findAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BaseException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(memberId);
    }
}
