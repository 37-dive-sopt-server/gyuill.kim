package org.sopt.domain.member.domain.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;

class MemberValidatorTest {

	private MemberValidator memberValidator;

	@BeforeEach
	void setUp() {
		memberValidator = new MemberValidator();
	}

	@Test
	@DisplayName("유효한 생년월일로 검증 성공")
	void validateBirthDate_Valid() {
		// given
		LocalDate validBirthDate = LocalDate.of(2000, 1, 1); // 24세

		// when & then
		assertThatNoException().isThrownBy(() ->
			memberValidator.validateBirthDate(validBirthDate)
		);
	}

	@ParameterizedTest(name = "{0}")
	@DisplayName("생년월일 검증 실패 케이스")
	@MethodSource("provideBirthDateInvalidCases")
	void validateBirthDate_InvalidCases_ThrowsException(String testName, LocalDate birthDate, ErrorCode expectedErrorCode) {
		// when & then
		assertThatThrownBy(() -> memberValidator.validateBirthDate(birthDate))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", expectedErrorCode);
	}

	private static Stream<Arguments> provideBirthDateInvalidCases() {
		return Stream.of(
			Arguments.of("null인 경우", null, ErrorCode.BIRTH_DATE_REQUIRED),
			Arguments.of("미래인 경우", LocalDate.now().plusDays(1), ErrorCode.BIRTH_DATE_FUTURE),
			Arguments.of("20세 미만인 경우", LocalDate.now().minusYears(19), ErrorCode.AGE_UNDER_20)
		);
	}

	@ParameterizedTest
	@DisplayName("경계값 테스트: 나이 정확히 20세")
	@CsvSource({
		"20, true",   // 정확히 20세 (유효)
		"19, false",  // 19세 (무효)
		"21, true",   // 21세 (유효)
		"25, true",   // 25세 (유효)
		"50, true"    // 50세 (유효)
	})
	void validateBirthDate_BoundaryTest(int age, boolean shouldPass) {
		// given
		LocalDate birthDate = LocalDate.now().minusYears(age);

		// when & then
		if (shouldPass) {
			assertThatNoException().isThrownBy(() ->
				memberValidator.validateBirthDate(birthDate)
			);
		} else {
			assertThatThrownBy(() -> memberValidator.validateBirthDate(birthDate))
				.isInstanceOf(MemberException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.AGE_UNDER_20);
		}
	}

	@Test
	@DisplayName("오늘이 정확히 20번째 생일인 경우 - 유효")
	void validateBirthDate_Exactly20thBirthday() {
		// given - 오늘이 정확히 20번째 생일
		LocalDate birthDate = LocalDate.now().minusYears(20);

		// when & then
		assertThatNoException().isThrownBy(() ->
			memberValidator.validateBirthDate(birthDate)
		);
	}

	@Test
	@DisplayName("20년 전 생년월일 - 단순 연도 차이로 20세 인정")
	void validateBirthDate_TwentyYearsAgo() {
		// given - 20년 전 (단순 연도 차이 계산으로 20세)
		LocalDate birthDate = LocalDate.now().minusYears(20).plusDays(1);

		// when & then - Validator는 단순히 year 차이만 계산하므로 통과
		assertThatNoException().isThrownBy(() ->
			memberValidator.validateBirthDate(birthDate)
		);
	}

	@Test
	@DisplayName("유효한 회원 생성 - createValidatedMember")
	void createValidatedMember_Success() {
		// given
		String password = "encodedPassword";
		String name = "Valid User";
		LocalDate birthDate = LocalDate.of(1995, 5, 15);
		String email = "valid@example.com";
		Gender gender = Gender.MALE;

		// when
		Member member = memberValidator.createValidatedMember(password, name, birthDate, email, gender);

		// then
		assertThat(member).isNotNull();
		assertThat(member.getName()).isEqualTo(name);
		assertThat(member.getEmail()).isEqualTo(email);
		assertThat(member.getBirthDate()).isEqualTo(birthDate);
		assertThat(member.getGender()).isEqualTo(gender);
	}

	@ParameterizedTest(name = "{0}")
	@DisplayName("생년월일 검증 실패 시 회원 생성 실패")
	@MethodSource("provideInvalidMemberCreationCases")
	void createValidatedMember_InvalidBirthDate_ThrowsException(String testName, LocalDate birthDate, ErrorCode expectedErrorCode) {
		// when & then
		assertThatThrownBy(() ->
			memberValidator.createValidatedMember(
				"password",
				"Test User",
				birthDate,
				"test@example.com",
				Gender.MALE
			)
		).isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", expectedErrorCode);
	}

	private static Stream<Arguments> provideInvalidMemberCreationCases() {
		return Stream.of(
			Arguments.of("null 생년월일", null, ErrorCode.BIRTH_DATE_REQUIRED),
			Arguments.of("미래 생년월일", LocalDate.now().plusDays(10), ErrorCode.BIRTH_DATE_FUTURE),
			Arguments.of("20세 미만", LocalDate.now().minusYears(18), ErrorCode.AGE_UNDER_20)
		);
	}

	@Test
	@DisplayName("여러 연령대 회원 생성 테스트")
	void createValidatedMember_VariousAges() {
		// given & when
		Member age20 = memberValidator.createValidatedMember(
			"pw", "Age20", LocalDate.now().minusYears(20), "age20@example.com", Gender.MALE);

		Member age30 = memberValidator.createValidatedMember(
			"pw", "Age30", LocalDate.now().minusYears(30), "age30@example.com", Gender.FEMALE);

		Member age50 = memberValidator.createValidatedMember(
			"pw", "Age50", LocalDate.now().minusYears(50), "age50@example.com", Gender.MALE);

		// then
		assertThat(age20).isNotNull();
		assertThat(age30).isNotNull();
		assertThat(age50).isNotNull();
	}
}
