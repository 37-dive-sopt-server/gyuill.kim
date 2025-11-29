package org.sopt.domain.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
	@NotBlank(message = "Refresh Token은 필수입니다")
	String refreshToken
) {
}
