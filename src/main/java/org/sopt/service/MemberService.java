package org.sopt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.sopt.domain.Gender;
import org.sopt.domain.Member;

public interface MemberService {
	Long join(String name, LocalDate birthDate, String email, Gender gender);
	Optional<Member> findOne(Long memberId);
	List<Member> findAllMembers();
	boolean deleteMember(String email);
}
