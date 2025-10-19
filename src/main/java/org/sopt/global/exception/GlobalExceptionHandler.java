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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 커스텀 예외 처리
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<CommonApiResponse<Void>> handleBaseException(BaseException e) {
		ErrorCode errorCode = (ErrorCode) e.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(CommonApiResponse.fail(errorCode));
	}

	// 입력 값 검증 실패 처리 (Member validation 등)
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
		Map<String, String> error = Map.of("message", e.getMessage());
		return CommonApiResponse.failWithDetails(ErrorCode.INVALID_INPUT, error);
	}

	// JSON 파싱 실패 처리 (enum, 날짜 형식 등)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleMessageNotReadableException(HttpMessageNotReadableException e) {
		Map<String, String> errorDetails = new HashMap<>();

		if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
			String fieldName = invalidFormatException.getPath().stream()
				.map(JsonMappingException.Reference::getFieldName)
				.collect(Collectors.joining("."));
			errorDetails.put(fieldName, "올바른 형식이 아닙니다");
		} else {
			errorDetails.put("body", "요청 데이터를 읽을 수 없습니다");
		}

		return CommonApiResponse.failWithDetails(ErrorCode.INVALID_FORMAT, errorDetails);
	}

	// 그 외 모든 예외 처리
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public CommonApiResponse<Void> handleException(Exception e) {
		return CommonApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
	}
}
