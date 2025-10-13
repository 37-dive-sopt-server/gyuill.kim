package org.sopt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.sopt.controller.MemberController;
import org.sopt.domain.Gender;
import org.sopt.domain.Member;

public class Main {
	public static void main(String[] args) {
		MemberController memberController = new MemberController();

		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("\n✨ --- DIVE SOPT 회원 관리 서비스 --- ✨");
			System.out.println("---------------------------------");
			System.out.println("1️⃣. 회원 등록 ➕");
			System.out.println("2️⃣. ID로 회원 조회 🔍");
			System.out.println("3️⃣. 전체 회원 조회 📋");
			System.out.println("4️⃣. 종료 🚪");
			System.out.println("---------------------------------");
			System.out.print("메뉴를 선택하세요: ");

			String choice = scanner.nextLine();

			switch (choice) {
				case "1":
					System.out.print("등록할 회원 이름을 입력하세요: ");
					String name = scanner.nextLine();
					if (name.trim().isEmpty()) {
						System.out.println("⚠️ 이름을 입력해주세요.");
						continue;
					}

					System.out.print("생년월일을 입력하세요 (YYYY-MM-DD): ");
					String birthDateStr = scanner.nextLine();
					LocalDate birthDate;
					try {
						birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
					} catch (DateTimeParseException e) {
						System.out.println("❌ 유효하지 않은 날짜 형식입니다. YYYY-MM-DD 형식으로 입력해주세요.");
						continue;
					}

					System.out.print("이메일을 입력하세요: ");
					String email = scanner.nextLine();
					if (email.trim().isEmpty()) {
						System.out.println("⚠️ 이메일을 입력해주세요.");
						continue;
					}

					System.out.print("성별을 선택하세요 (1: 남성, 2: 여성, 3: 기타): ");
					String genderChoice = scanner.nextLine();
					Gender gender;
					switch (genderChoice) {
						case "1":
							gender = Gender.MALE;
							break;
						case "2":
							gender = Gender.FEMALE;
							break;
						case "3":
							gender = Gender.OTHER;
							break;
						default:
							System.out.println("⚠️ 유효하지 않은 성별 선택입니다.");
							continue;
					}

					try {
						Long createdId = memberController.createMember(name, birthDate, email, gender);
						if (createdId != null) {
							System.out.println("✅ 회원 등록 완료 (ID: " + createdId + ")");
						} else {
							System.out.println("❌ 회원 등록 실패");
						}
					} catch (IllegalStateException e) {
						System.out.println("❌ " + e.getMessage());
					}
					break;
				case "2":
					System.out.print("조회할 회원 ID를 입력하세요: ");
					try {
						Long id = Long.parseLong(scanner.nextLine());
						Optional<Member> foundMember = memberController.findMemberById(id);
						if (foundMember.isPresent()) {
							Member member = foundMember.get();
							System.out.println("✅ 조회된 회원 정보:");
							System.out.println("   ID: " + member.getId());
							System.out.println("   이름: " + member.getName());
							System.out.println("   생년월일: " + member.getBirthDate());
							System.out.println("   이메일: " + member.getEmail());
							System.out.println("   성별: " + member.getGender().getDescription());
						} else {
							System.out.println("⚠️ 해당 ID의 회원을 찾을 수 없습니다.");
						}
					} catch (NumberFormatException e) {
						System.out.println("❌ 유효하지 않은 ID 형식입니다. 숫자를 입력해주세요.");
					}
					break;
				case "3":
					List<Member> allMembers = memberController.getAllMembers();
					if (allMembers.isEmpty()) {
						System.out.println("ℹ️ 등록된 회원이 없습니다.");
					}
					else {
						System.out.println("--- 📋 전체 회원 목록 📋 ---");
						for (Member member : allMembers) {
							System.out.println("👤 ID=" + member.getId() +
								", 이름=" + member.getName() +
								", 생년월일=" + member.getBirthDate() +
								", 이메일=" + member.getEmail() +
								", 성별=" + member.getGender().getDescription());
						}
						System.out.println("--------------------------");
					}
					break;
				case "4":
					System.out.println("👋 서비스를 종료합니다. 안녕히 계세요!");
					scanner.close();
					return;
				default:
					System.out.println("🚫 잘못된 메뉴 선택입니다. 다시 시도해주세요.");
			}
		}
	}
}