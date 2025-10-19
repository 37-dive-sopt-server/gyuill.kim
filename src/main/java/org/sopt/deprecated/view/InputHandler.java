package org.sopt.deprecated.view;

import java.time.LocalDate;
import java.util.Scanner;

import org.sopt.member.domain.entity.Gender;
import org.sopt.member.application.validator.MemberValidator;

@Deprecated
public class InputHandler {
	private final Scanner scanner;
	private final MemberValidator validator;

	public InputHandler(MemberValidator validator) {
		this.scanner = new Scanner(System.in);
		this.validator = validator;
	}

	public String readMenuChoice() {
		return scanner.nextLine();
	}

	public String readName() {
		System.out.print("등록할 회원 이름을 입력하세요: ");
		String name = scanner.nextLine().trim();
		validator.validateName(name);
		return name;
	}

	public LocalDate readBirthDate() {
		System.out.print("생년월일을 입력하세요 (YYYY-MM-DD): ");
		String input = scanner.nextLine().trim();
		return validator.validateAndParseBirthDate(input);
	}

	public String readEmail() {
		System.out.print("이메일을 입력하세요: ");
		String email = scanner.nextLine().trim();
		validator.validateEmail(email);
		return email;
	}

	public Gender readGender() {
		System.out.print("성별을 선택하세요 (1: 남성, 2: 여성, 3: 기타): ");
		String choice = scanner.nextLine().trim();
		return validator.validateAndParseGender(choice);
	}

	public Long readMemberId() {
		System.out.print("조회할 회원 ID를 입력하세요: ");
		String input = scanner.nextLine().trim();
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("유효하지 않은 ID 형식입니다. 숫자를 입력해주세요.");
		}
	}

	public String readEmailForDelete() {
		System.out.print("삭제할 회원 이메일을 입력하세요: ");
		return scanner.nextLine().trim();
	}

	public void close() {
		scanner.close();
	}
}
