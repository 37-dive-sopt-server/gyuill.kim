package org.sopt.member.application.service;

import java.util.List;

import org.sopt.member.application.dto.MemberCreateRequest;
import org.sopt.member.application.dto.MemberResponse;

public interface MemberService {
	MemberResponse join(MemberCreateRequest request);
	MemberResponse findMember(Long memberId);
	List<MemberResponse> findAllMembers();
	void deleteMember(String email);
}
