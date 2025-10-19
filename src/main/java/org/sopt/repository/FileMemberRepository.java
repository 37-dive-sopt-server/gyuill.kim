package org.sopt.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sopt.domain.Member;
import org.sopt.storage.MemberFileStorage;
import org.springframework.stereotype.Repository;

@Repository
public class FileMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new HashMap<>();
	private final Map<String, Member> emailIndex = new HashMap<>();
	private final MemberFileStorage fileStorage;
	private long sequence = 1L;

	public FileMemberRepository(String filePath) {
		this.fileStorage = new MemberFileStorage(filePath);
		loadMembers();
		initializeSequence();
	}

	@Override
	public Long generateNextId() {
		return sequence++;
	}

	@Override
	public void save(Member member) {
		store.put(member.id(), member);
		emailIndex.put(member.email(), member);
		persistToFile();
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
		return Optional.ofNullable(emailIndex.get(email));
	}

	@Override
	public boolean deleteByEmail(String email) {
		Optional<Member> member = findByEmail(email);
		if (member.isPresent()) {
			Member m = member.get();
			store.remove(m.id());
			emailIndex.remove(m.email());
			persistToFile();
			return true;
		}
		return false;
	}

	private void loadMembers() {
		List<Member> members = fileStorage.load();
		for (Member member : members) {
			store.put(member.id(), member);
			emailIndex.put(member.email(), member);
		}
	}

	private void initializeSequence() {
		sequence = store.keySet().stream()
			.max(Long::compareTo)
			.map(maxId -> maxId + 1)
			.orElse(1L);
	}

	private void persistToFile() {
		List<Member> members = new ArrayList<>(store.values());
		fileStorage.save(members);
	}
}
