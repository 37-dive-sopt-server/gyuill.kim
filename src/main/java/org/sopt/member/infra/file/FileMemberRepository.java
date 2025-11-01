package org.sopt.member.infra.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.sopt.member.domain.entity.Member;
import org.sopt.member.domain.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class FileMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new HashMap<>();
	private final Map<String, Member> emailIndex = new HashMap<>();
	private final MemberFileStorage fileStorage;
	private final AtomicLong sequence = new AtomicLong(1L);

	public FileMemberRepository(@Value("${member.file.path}") String filePath) {
		this.fileStorage = new MemberFileStorage(filePath);
		loadMembers();
		initializeSequence();
	}

	@Override
	public Long generateNextId() {
		return sequence.getAndIncrement();
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
		long nextId = store.keySet().stream()
			.max(Long::compareTo)
			.map(maxId -> maxId + 1)
			.orElse(1L);
		sequence.set(nextId);
	}

	private void persistToFile() {
		List<Member> members = new ArrayList<>(store.values());
		fileStorage.save(members);
	}
}
