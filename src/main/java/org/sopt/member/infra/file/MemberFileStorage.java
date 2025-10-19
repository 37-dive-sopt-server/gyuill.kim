package org.sopt.member.infra.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sopt.member.domain.entity.Member;
import org.sopt.global.exception.DataAccessException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MemberFileStorage {
	private final String filePath;
	private final ObjectMapper objectMapper;

	public MemberFileStorage(String filePath) {
		this.filePath = filePath;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	public List<Member> load() {
		File file = new File(filePath);
		if (!file.exists()) {
			// 파일이 없으면 빈 목록 반환
			return new ArrayList<>();
		}

		try {
			List<Member> members = objectMapper.readValue(file, new TypeReference<List<Member>>() {});
			return members != null ? members : new ArrayList<>();
		} catch (JsonProcessingException e) {
			throw new DataAccessException("데이터 파일 형식이 올바르지 않습니다: " + filePath, e);
		} catch (IOException e) {
			throw new DataAccessException("파일을 읽는 중 오류가 발생했습니다: " + filePath, e);
		}
	}

	public void save(List<Member> members) {
		File file = new File(filePath);
		File tempFile = new File(filePath + ".tmp");
		File parentDir = file.getParentFile();

		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		try {
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
