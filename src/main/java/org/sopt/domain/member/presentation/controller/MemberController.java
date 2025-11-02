package org.sopt.domain.member.presentation.controller;

import java.util.List;

import org.sopt.global.annotation.ApiExceptions;
import org.sopt.global.annotation.AutoApiResponse;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.success.SuccessCode;
import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/members")
@AutoApiResponse
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping
	@SuccessCodeAnnotation(SuccessCode.MEMBER_CREATED)
	@Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
	@ApiExceptions({ErrorCode.DUPLICATE_EMAIL, ErrorCode.INVALID_INPUT, ErrorCode.INVALID_FORMAT})
	public MemberResponse createMember(
		@Parameter(description = "회원 가입 정보", required = true)
		@Valid @RequestBody MemberCreateRequest request
	) {
		return memberService.join(request);
	}

	@GetMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.MEMBER_VIEW)
	@Operation(summary = "회원 조회", description = "ID로 특정 회원의 정보를 조회합니다.")
	@ApiExceptions({ErrorCode.MEMBER_NOT_FOUND})
	public MemberResponse getMemberById(
		@Parameter(description = "회원 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		return memberService.findMember(id);
	}

	@GetMapping
	@SuccessCodeAnnotation(SuccessCode.MEMBER_VIEW)
	@Operation(summary = "전체 회원 조회", description = "등록된 모든 회원의 정보를 페이징하여 조회합니다.")
	public Page<MemberResponse> getAllMembers(
		@PageableDefault(size = 20) Pageable pageable
	) {
		return memberService.findAllMembers(pageable);
	}

	@DeleteMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.MEMBER_DELETED)
	@Operation(summary = "회원 삭제", description = "ID로 특정 회원을 삭제합니다.")
	@ApiExceptions({ErrorCode.MEMBER_NOT_FOUND})
	public void deleteMember(
		@Parameter(description = "회원 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		memberService.deleteMember(id);
	}
}
