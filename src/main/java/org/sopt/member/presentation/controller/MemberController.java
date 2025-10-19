package org.sopt.member.presentation.controller;

import java.util.List;

import org.sopt.global.annotation.AutoApiResponse;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.success.SuccessCode;
import org.sopt.member.application.dto.MemberCreateRequest;
import org.sopt.member.application.dto.MemberResponse;
import org.sopt.member.application.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@AutoApiResponse
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@SuccessCodeAnnotation(SuccessCode.MEMBER_CREATED)
	public MemberResponse createMember(@RequestBody MemberCreateRequest request) {
		return memberService.join(request);
	}

	@GetMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.MEMBER_VIEW)
	public MemberResponse getMemberById(@PathVariable Long id) {
		return memberService.findMember(id);
	}

	@GetMapping
	@SuccessCodeAnnotation(SuccessCode.MEMBER_VIEW)
	public List<MemberResponse> getAllMembers() {
		return memberService.findAllMembers();
	}

	@DeleteMapping("/{email}")
	@SuccessCodeAnnotation(SuccessCode.MEMBER_DELETED)
	public void deleteMember(@PathVariable String email) {
		memberService.deleteMember(email);
	}
}
