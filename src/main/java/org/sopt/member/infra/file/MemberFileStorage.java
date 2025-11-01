package org.sopt.member.infra.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.sopt.member.domain.entity.Gender;
import org.sopt.member.domain.entity.Member;
import org.sopt.global.exception.DataAccessException;
import org.sopt.global.response.error.ErrorCode;

public class MemberFileStorage {
	private static final String DELIMITER = ",";
	private final String filePath;

	public MemberFileStorage(String filePath) {
		this.filePath = filePath;
	}

	public List<Member> load() {
		File file = new File(filePath);
		if (!file.exists()) {
			return new ArrayList<>();
		}

		List<Member> members = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				members.add(parseMember(line));
			}
			return members;
		} catch (IOException e) {
			throw new DataAccessException(ErrorCode.DATA_READ_ERROR);
		} catch (Exception e) {
			throw new DataAccessException(ErrorCode.DATA_PARSE_ERROR);
		}
	}

	public void save(List<Member> members) {
		Path targetPath = Path.of(filePath);
		Path tempPath = Path.of(filePath + ".tmp");
		File parentDir = targetPath.toFile().getParentFile();

		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath.toFile()))) {
			for (Member member : members) {
				writer.write(formatMember(member));
				writer.newLine();
			}
		} catch (IOException e) {
			try {
				Files.deleteIfExists(tempPath);
			} catch (IOException ex) {
			}
			throw new DataAccessException(ErrorCode.DATA_WRITE_ERROR);
		}

		try {
			Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			try {
				Files.deleteIfExists(tempPath);
			} catch (IOException ex) {
			}
			throw new DataAccessException(ErrorCode.DATA_MOVE_ERROR);
		}
	}

	private Member parseMember(String line) {
		String[] parts = line.split(DELIMITER, -1);
		if (parts.length != 5) {
			throw new IllegalArgumentException("잘못된 데이터 형식입니다");
		}

		Long id = Long.parseLong(parts[0]);
		String name = parts[1];
		LocalDate birthDate = LocalDate.parse(parts[2]);
		String email = parts[3];
		Gender gender = Gender.valueOf(parts[4]);

		return new Member(id, name, birthDate, email, gender);
	}

	private String formatMember(Member member) {
		return String.join(DELIMITER,
			member.id().toString(),
			member.name(),
			member.birthDate().toString(),
			member.email(),
			member.gender().name()
		);
	}
}
