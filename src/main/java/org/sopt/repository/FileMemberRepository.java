package org.sopt.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sopt.domain.Member;
import org.sopt.exception.DataAccessException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class FileMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new HashMap<>();
	private final Map<String, Member> emailIndex = new HashMap<>();
	private final String filePath;
	private final ObjectMapper objectMapper;
	private long sequence = 1L;

	public FileMemberRepository(String filePath) {
		this.filePath = filePath;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		loadFromFile();
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
		return Optional.ofNullable(emailIndex.get(email));
	}

	@Override
	public boolean deleteByEmail(String email) {
		Optional<Member> member = findByEmail(email);
		if (member.isPresent()) {
			Member m = member.get();
			store.remove(m.id());
			emailIndex.remove(m.email());
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

		try {
			List<Member> members = objectMapper.readValue(file, new TypeReference<List<Member>>() {});
			if (members != null) {
				for (Member member : members) {
					store.put(member.id(), member);
					emailIndex.put(member.email(), member);
				}
			}
		} catch (JsonProcessingException e) {
			throw new DataAccessException("데이터 파일 형식이 올바르지 않습니다: " + filePath, e);
		} catch (IOException e) {
			throw new DataAccessException("파일을 읽는 중 오류가 발생했습니다: " + filePath, e);
		}
	}

	private void initializeSequence() {
		sequence = store.keySet().stream()
			.max(Long::compareTo)
			.map(maxId -> maxId + 1)
			.orElse(1L);
	}

	private void saveToFile() {
		File file = new File(filePath);
		File tempFile = new File(filePath + ".tmp");
		File parentDir = file.getParentFile();

		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		try {
			List<Member> members = new ArrayList<>(store.values());
			objectMapper.writeValue(tempFile, members);

			// 원자적 교체
			if (file.exists() && !file.delete()) {
				throw new IOException("기존 파일을 삭제할 수 없습니다");
			}
			if (!tempFile.renameTo(file)) {
				throw new IOException("임시 파일을 원본 파일로 이동할 수 없습니다");
			}
		} catch (IOException e) {
			if (tempFile.exists()) {
				tempFile.delete();
			}
			throw new DataAccessException("파일을 저장하는 중 오류가 발생했습니다: " + filePath, e);
		}
	}
}
