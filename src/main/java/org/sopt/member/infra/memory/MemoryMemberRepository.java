package org.sopt.member.infra.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sopt.member.domain.entity.Member;
import org.sopt.member.domain.repository.MemberRepository;

// @Repository
public class MemoryMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new HashMap<>();
	private long sequence = 1L;

	@Override
	public Long generateNextId() {
		return sequence++;
	}

	@Override
	public void save(Member member) {
		store.put(member.id(), member);
	}

	@Override
	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<Member> findAll() {
		return new ArrayList<>(store.values());
	}

	@Override
	public Optional<Member> findByEmail(String email) {
		return store.values().stream()
			.filter(member -> member.email().equals(email))
			.findFirst();
	}

	@Override
	public boolean deleteByEmail(String email) {
		Optional<Member> member = findByEmail(email);
		if (member.isPresent()) {
			store.remove(member.get().id());
			return true;
		}
		return false;
	}
}
