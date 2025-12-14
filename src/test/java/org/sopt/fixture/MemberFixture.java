package org.sopt.fixture;

import java.time.LocalDate;

import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

	private static final String DEFAULT_PASSWORD = "encodedPassword123";
	private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1995, 5, 15);
	private static final Gender DEFAULT_GENDER = Gender.MALE;
	private static final String DEFAULT_PROFILE_IMAGE_URL = "https://example.com/profile.jpg";

	public static Member createLocalMember(String email, String name) {
		return createLocalMember(email, name, DEFAULT_PASSWORD, DEFAULT_BIRTH_DATE, DEFAULT_GENDER);
	}

	public static Member createLocalMember(String email, String name, Gender gender) {
		return createLocalMember(email, name, DEFAULT_PASSWORD, DEFAULT_BIRTH_DATE, gender);
	}

	private static Member createLocalMember(String email, String name, String password,
	                                        LocalDate birthDate, Gender gender) {
		return Member.create(password, name, birthDate, email, gender);
	}

	public static Member createLocalMemberWithAge(String email, String name, int age) {
		return createLocalMember(
			email,
			name,
			DEFAULT_PASSWORD,
			LocalDate.now().minusYears(age),
			DEFAULT_GENDER
		);
	}

	public static Member createSocialMember(String email, String name, SocialProvider provider) {
		return Member.createSocialMember(
			email,
			name,
			provider,
			"provider-id-" + email,
			DEFAULT_PROFILE_IMAGE_URL
		);
	}

	public static Member createMemberWithId(Long id, String email, String name) {
		Member member = createLocalMember(email, name);
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

	public static Member createMemberWithPassword(String email, String name, String encodedPassword) {
		return createLocalMember(email, name, encodedPassword, DEFAULT_BIRTH_DATE, DEFAULT_GENDER);
	}
}
