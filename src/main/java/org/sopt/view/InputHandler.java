package org.sopt.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import org.sopt.domain.Gender;

public class InputHandler {
	private final Scanner scanner;

	public InputHandler(Scanner scanner) {
		this.scanner = scanner;
	}

	public String readMenuChoice() {
		return scanner.nextLine();
	}

	private String readNonEmptyString(String prompt, String errorMessage) {
		System.out.print(prompt);
		String input = scanner.nextLine();
		if (input.trim().isEmpty()) {
			throw new IllegalArgumentException(errorMessage);
		}
		return input;
	}

	public String readName() {
		return readNonEmptyString(
			"등록할 회원 이름을 입력하세요: ",
			"이름을 입력해주세요."
		);
	}

	public LocalDate readBirthDate() {
		System.out.print("생년월일을 입력하세요 (YYYY-MM-DD): ");
		String input = scanner.nextLine();
		try {
			return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("유효하지 않은 날짜 형식입니다. YYYY-MM-DD 형식으로 입력해주세요.");
		}
	}

	public String readEmail() {
		return readNonEmptyString(
			"이메일을 입력하세요: ",
			"이메일을 입력해주세요."
		);
	}

	public Gender readGender() {
		System.out.print("성별을 선택하세요 (1: 남성, 2: 여성, 3: 기타): ");
		String choice = scanner.nextLine();
		switch (choice) {
			case "1":
				return Gender.MALE;
			case "2":
				return Gender.FEMALE;
			case "3":
				return Gender.OTHER;
			default:
				throw new IllegalArgumentException("유효하지 않은 성별 선택입니다.");
		}
	}

	public Long readMemberId() {
		System.out.print("조회할 회원 ID를 입력하세요: ");
		String input = scanner.nextLine();
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("유효하지 않은 ID 형식입니다. 숫자를 입력해주세요.");
		}
	}

	public String readEmailForDelete() {
		return readNonEmptyString(
			"삭제할 회원 이메일을 입력하세요: ",
			"이메일을 입력해주세요."
		);
	}

	public void close() {
		scanner.close();
	}
}
