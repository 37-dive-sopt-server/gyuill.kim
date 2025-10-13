package org.sopt.repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sopt.domain.Member;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class FileMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new HashMap<>();
	private final String filePath;
	private final Gson gson;

	public FileMemberRepository(String filePath) {
		this.filePath = filePath;
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		loadFromFile();
	}

	@Override
	public void save(Member member) {
		store.put(member.id(), member);
		saveToFile();
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
			saveToFile();
			return true;
		}
		return false;
	}

	private void loadFromFile() {
		File file = new File(filePath);
		if (!file.exists()) {
			// 파일이 없으면 빈 저장소로 시작
			return;
		}

		try (FileReader reader = new FileReader(file)) {
			List<Member> members = gson.fromJson(reader, new TypeToken<List<Member>>() {}.getType());
			if (members != null) {
				for (Member member : members) {
					store.put(member.id(), member);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("파일을 읽는 중 오류가 발생했습니다: " + filePath, e);
		}
	}

	private void saveToFile() {
		File file = new File(filePath);
		File parentDir = file.getParentFile();

		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		try (FileWriter writer = new FileWriter(file)) {
			List<Member> members = new ArrayList<>(store.values());
			gson.toJson(members, writer);
		} catch (IOException e) {
			throw new RuntimeException("파일을 저장하는 중 오류가 발생했습니다: " + filePath, e);
		}
	}
}
