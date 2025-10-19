package org.sopt.member.domain.repository;

import java.util.List;
import java.util.Optional;

import org.sopt.member.domain.entity.Member;

public interface MemberRepository {
	Long generateNextId();
	void save(Member member);
	Optional<Member> findById(Long id);
	List<Member> findAll();
	Optional<Member> findByEmail(String email);
	boolean deleteByEmail(String email);
}
