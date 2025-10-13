package org.sopt.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sopt.domain.Member;

public class MemoryMemberRepository implements MemberRepository {

	private static final Map<Long, Member> store = new HashMap<>();

	public void save(Member member) {
		store.put(member.id(), member);
	}


	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}


	public List<Member> findAll() {
		return new ArrayList<>(store.values());
	}

	public Optional<Member> findByEmail(String email) {
		return store.values().stream()
			.filter(member -> member.email().equals(email))
			.findFirst();
	}

	public boolean deleteByEmail(String email) {
		Optional<Member> member = findByEmail(email);
		if (member.isPresent()) {
			store.remove(member.get().id());
			return true;
		}
		return false;
	}
}
