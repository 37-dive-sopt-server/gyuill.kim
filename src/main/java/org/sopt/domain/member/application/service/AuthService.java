package org.sopt.domain.member.application.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;

	public MemberResponse login(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			throw new IllegalArgumentException("Authorization 헤더가 없습니다.");
		}

		if (!authorizationHeader.startsWith("Basic ")) {
			throw new IllegalArgumentException("Basic 인증 형식이 아닙니다.");
		}

		String token = authorizationHeader.substring("Basic ".length()).trim();
		String decoded;
		try {
			decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Authorization 헤더가 Base64 형식이 아닙니다.");
		}

		int idx = decoded.indexOf(":");
		if (idx < 0) {
			throw new IllegalArgumentException("이메일과 비밀번호 구분자(:)가 없습니다.");
		}

		String email = decoded.substring(0, idx);
		String password = decoded.substring(idx + 1);

		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));


		if (member.getPassword() == null || !member.getPassword().equals(password)) {
			throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		return MemberResponse.fromEntity(member);
	}

	public MemberResponse loginWithCredentials(String email, String password) {
		if (email == null || email.isBlank() || password == null ||
			password.isBlank()) {
			throw new IllegalArgumentException("이메일과 비밀번호를 모두 입력해주세요.");
		}

		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

		if (member.getPassword() == null || !
			member.getPassword().equals(password)) {
			throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		return MemberResponse.fromEntity(member);

	}
}

