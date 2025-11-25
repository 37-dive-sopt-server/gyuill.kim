package org.sopt.domain.auth.application.dto;

public record TokenPair(
	String accessToken,
	String refreshToken
) {
}
