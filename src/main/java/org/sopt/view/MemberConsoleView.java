package org.sopt.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.sopt.controller.MemberController;
import org.sopt.domain.Gender;
import org.sopt.domain.Member;
import org.sopt.exception.DataAccessException;

public class MemberConsoleView {
	private final MemberController controller;
	private final InputHandler inputHandler;

	public MemberConsoleView(MemberController controller, InputHandler inputHandler) {
		this.controller = controller;
		this.inputHandler = inputHandler;
	}

	public void run() {
		while (true) {
			printMenu();
			String choice = inputHandler.readMenuChoice();

			boolean shouldExit = processMenu(choice);
			if (shouldExit) {
				break;
			}
		}
	}

	private void printMenu() {
		System.out.println("\n✨ --- DIVE SOPT 회원 관리 서비스 --- ✨");
		System.out.println("---------------------------------");
		System.out.println("1. 회원 등록");
		System.out.println("2. ID로 회원 조회");
		System.out.println("3. 전체 회원 조회");
		System.out.println("4. 회원 삭제");
		System.out.println("5. 종료");
		System.out.println("---------------------------------");
		System.out.print("메뉴를 선택하세요: ");
	}

	private boolean processMenu(String choice) {
		return switch (choice) {
			case "1" -> {
				handleMemberRegistration();
				yield false;
			}
			case "2" -> {
				handleFindMemberById();
				yield false;
			}
			case "3" -> {
				handleFindAllMembers();
				yield false;
			}
			case "4" -> {
				handleDeleteMember();
				yield false;
			}
			case "5" -> {
				handleExit();
				yield true;
			}
			default -> {
				printErrorMessage("잘못된 메뉴 선택입니다. 다시 시도해주세요.");
				yield false;
			}
		};
	}

	private void handleMemberRegistration() {
		try {
			String name = inputHandler.readName();
			LocalDate birthDate = inputHandler.readBirthDate();
			String email = inputHandler.readEmail();
			Gender gender = inputHandler.readGender();

			Long createdId = controller.createMember(name, birthDate, email, gender);
			if (createdId != null) {
				printSuccessMessage("회원 등록 완료 (ID: " + createdId + ")");
			} else {
				printErrorMessage("회원 등록 실패");
			}
		} catch (IllegalArgumentException | IllegalStateException e) {
			printWarningMessage(e.getMessage());
		} catch (DataAccessException e) {
			printErrorMessage(e.getMessage());
		}
	}

	private void handleFindMemberById() {
		try {
			Long id = inputHandler.readMemberId();
			Optional<Member> foundMember = controller.findMemberById(id);
			if (foundMember.isPresent()) {
				printMemberInfo(foundMember.get());
			} else {
				printWarningMessage("해당 ID의 회원을 찾을 수 없습니다.");
			}
		} catch (IllegalArgumentException e) {
			printWarningMessage(e.getMessage());
		} catch (DataAccessException e) {
			printErrorMessage(e.getMessage());
		}
	}

	private void handleFindAllMembers() {
		try {
			List<Member> allMembers = controller.getAllMembers();
			if (allMembers.isEmpty()) {
				printInfoMessage("등록된 회원이 없습니다.");
			} else {
				printMemberList(allMembers);
			}
		} catch (DataAccessException e) {
			printErrorMessage(e.getMessage());
		}
	}

	private void handleDeleteMember() {
		try {
			String email = inputHandler.readEmailForDelete();
			boolean deleted = controller.deleteMember(email);
			if (deleted) {
				printSuccessMessage("회원 삭제 완료 (이메일: " + email + ")");
			} else {
				printWarningMessage("해당 이메일의 회원을 찾을 수 없습니다.");
			}
		} catch (IllegalArgumentException e) {
			printWarningMessage(e.getMessage());
		} catch (DataAccessException e) {
			printErrorMessage(e.getMessage());
		}
	}

	private void handleExit() {
		System.out.println("👋 서비스를 종료합니다. 안녕히 계세요!");
		inputHandler.close();
	}

	private void printSuccessMessage(String message) {
		System.out.println("[성공] " + message);
	}

	private void printWarningMessage(String message) {
		System.out.println("[경고] " + message);
	}

	private void printErrorMessage(String message) {
		System.out.println("[실패] " + message);
	}

	private void printInfoMessage(String message) {
		System.out.println("[정보] " + message);
	}

	private void printMemberInfo(Member member) {
		System.out.println("✅ 조회된 회원 정보:");
		System.out.println("   ID: " + member.id());
		System.out.println("   이름: " + member.name());
		System.out.println("   생년월일: " + member.birthDate());
		System.out.println("   이메일: " + member.email());
		System.out.println("   성별: " + member.gender().getDescription());
	}

	private void printMemberList(List<Member> members) {
		System.out.println("--- 📋 전체 회원 목록 📋 ---");
		for (Member member : members) {
			System.out.println("👤 ID=" + member.id() +
				", 이름=" + member.name() +
				", 생년월일=" + member.birthDate() +
				", 이메일=" + member.email() +
				", 성별=" + member.gender().getDescription());
		}
		System.out.println("--------------------------");
	}
}
