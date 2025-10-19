package org.sopt.global.response;

import org.sopt.global.response.success.SuccessType;
import org.sopt.global.response.error.ErrorType;

import io.swagger.v3.oas.annotations.media.Schema;

public class CommonApiResponse<T> {

	@Schema(description = "응답 코드", example = "M001")
	private final String code;

	@Schema(description = "응답 메시지", example = "회원 정보 조회 성공")
	private final String message;

	@Schema(description = "응답 데이터", nullable = true)
	private final T data;

	public static <T> CommonApiResponse<T> success(SuccessType successCode, T data) {
		return new CommonApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
	}

	public static CommonApiResponse<Void> success(SuccessType successCode) {
		return new CommonApiResponse<>(successCode.getCode(), successCode.getMessage(), null);
	}

	public static CommonApiResponse<Void> fail(ErrorType errorType) {
		return new CommonApiResponse<>(errorType.getCode(), errorType.getMessage(), null);
	}

	public static <T> CommonApiResponse<T> failWithDetails(ErrorType errorType, T details) {
		return new CommonApiResponse<>(errorType.getCode(), errorType.getMessage(), details);
	}

	private CommonApiResponse(String code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}
}
