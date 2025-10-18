package org.sopt;

import org.sopt.controller.MemberController;
import org.sopt.repository.FileMemberRepository;
import org.sopt.repository.MemberRepository;
import org.sopt.service.MemberService;
import org.sopt.service.MemberServiceImpl;
import org.sopt.validator.MemberValidator;
import org.sopt.view.InputHandler;
import org.sopt.view.MemberConsoleView;

public class Main {
	public static void main(String[] args) {
		MemberRepository repository = new FileMemberRepository("data/members.json");
		// MemberRepository repository2 = new MemoryMemberRepository();
		MemberService memberService = new MemberServiceImpl(repository);
		MemberController controller = new MemberController(memberService);
		MemberValidator validator = new MemberValidator(
			email -> repository.findByEmail(email).isPresent()
		);
		InputHandler inputHandler = new InputHandler(validator);
		MemberConsoleView view = new MemberConsoleView(controller, inputHandler);

		view.run();
	}
}