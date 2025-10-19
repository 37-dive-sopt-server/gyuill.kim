package org.sopt.controller;

import java.util.List;

import org.sopt.dto.MemberCreateRequest;
import org.sopt.dto.MemberResponse;
import org.sopt.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping
	public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
		MemberResponse response = memberService.join(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
		MemberResponse response = memberService.findMember(id);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<MemberResponse>> getAllMembers() {
		List<MemberResponse> responses = memberService.findAllMembers();
		return ResponseEntity.ok(responses);
	}

	@DeleteMapping("/{email}")
	public ResponseEntity<Void> deleteMember(@PathVariable String email) {
		memberService.deleteMember(email);
		return ResponseEntity.noContent().build();
	}
}
