package org.sopt.domain.member.presentation.controller;

import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.service.MemberService;
import org.sopt.global.annotation.ApiExceptions;
import org.sopt.global.annotation.AutoApiResponse;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
@AutoApiResponse
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

	private final MemberService memberService;

	@PostMapping
	@SuccessCodeAnnotation(SuccessCode.MEMBER_CREATED)
	@Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
	@ApiExceptions({ErrorCode.DUPLICATE_EMAIL, ErrorCode.INVALID_INPUT, ErrorCode.INVALID_FORMAT,
		ErrorCode.BIRTH_DATE_REQUIRED, ErrorCode.BIRTH_DATE_FUTURE, ErrorCode.AGE_UNDER_20})
	public MemberResponse createMember(
		@Parameter(description = "회원 가입 정보", required = true)
		@Valid @RequestBody MemberCreateRequest request
	) {
		return memberService.create(request);
	}

	@GetMapping("/{id}")
	@SuccessCodeAnnotation(SuccessCode.MEMBER_VIEW)
	@Operation(summary = "회원 조회", description = "ID로 특정 회원의 정보를 조회합니다.")
	@ApiExceptions({ErrorCode.MEMBER_NOT_FOUND})
	public MemberResponse getMemberById(
		@Parameter(description = "회원 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		return memberService.getMemberById(id);
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
