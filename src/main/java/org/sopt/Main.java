package org.sopt;

import java.util.Scanner;

import org.sopt.controller.MemberController;
import org.sopt.view.InputHandler;
import org.sopt.view.MemberConsoleView;

public class Main {
	public static void main(String[] args) {
		MemberController controller = new MemberController();
		InputHandler inputHandler = new InputHandler(new Scanner(System.in));
		MemberConsoleView view = new MemberConsoleView(controller, inputHandler);

		view.run();
	}
}