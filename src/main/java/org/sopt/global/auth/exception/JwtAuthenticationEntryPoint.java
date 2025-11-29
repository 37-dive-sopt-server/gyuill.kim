package org.sopt.global.auth.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.sopt.global.response.CommonApiResponse;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		CommonApiResponse<Void> errorResponse = CommonApiResponse.fail(ErrorCode.LOGIN_FAIL);

		String jsonResponse = objectMapper.writeValueAsString(errorResponse);
		response.getWriter().write(jsonResponse);
	}
}
