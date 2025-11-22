package org.sopt.domain.member.application.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import java.time.LocalDate;

import org.sopt.domain.member.domain.entity.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "회원 가입 요청")
public record MemberCreateRequest(
        @Schema(description = "회원 이름", example = "김솝트", requiredMode = REQUIRED)
        @NotBlank(message = "이름을 입력해주세요")
        String name,

        @Schema(description = "비밀번호", example = "password123!", requiredMode = REQUIRED)
        @NotBlank(message = "비밀번호를 입력해주세요")
        String password,

        @Schema(description = "생년월일 (yyyy-MM-dd)", example = "2000-01-01", requiredMode = REQUIRED)
        @NotNull(message = "생년월일을 입력해주세요")
        LocalDate birthDate,

        @Schema(description = "이메일 주소", example = "sopt@sopt.org", requiredMode = REQUIRED)
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "성별 (MALE/FEMALE/OTHER)", example = "MALE", requiredMode = REQUIRED)
        @NotNull(message = "성별을 선택해주세요")
        Gender gender
) {
}
