package org.sopt.service;

import java.util.List;

import org.sopt.dto.MemberCreateRequest;
import org.sopt.dto.MemberResponse;

public interface MemberService {
	MemberResponse join(MemberCreateRequest request);
	MemberResponse findMember (Long memberId);
	List<MemberResponse> findAllMembers();
	void deleteMember(String email);
}
