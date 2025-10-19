package org.sopt.global.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.global.response.CommonApiResponse;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 커스텀 예외 처리
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<?> handleBaseException(BaseException e) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(CommonApiResponse.fail(e.getErrorCode()));
	}

	// 입력 값 검증 실패 처리 (Member validation 등)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
		Map<String, String> error = Map.of("message", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(CommonApiResponse.failWithDetails(ErrorCode.INVALID_INPUT, error));
	}

	// JSON 파싱 실패 처리 (enum, 날짜 형식 등)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleMessageNotReadableException(HttpMessageNotReadableException e) {
		Map<String, String> errorDetails = new HashMap<>();

		if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
			String fieldName = invalidFormatException.getPath().stream()
				.map(JsonMappingException.Reference::getFieldName)
				.collect(Collectors.joining("."));
			errorDetails.put(fieldName, "올바른 형식이 아닙니다");
		} else {
			errorDetails.put("body", "요청 데이터를 읽을 수 없습니다");
		}

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(CommonApiResponse.failWithDetails(ErrorCode.INVALID_FORMAT, errorDetails));
	}

	// 그 외 모든 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception e) {
		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(CommonApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
