package org.sopt.fixture;

import java.time.LocalDate;

import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

	public static Member createLocalMember(String email, String name) {
		return Member.create(
			"encodedPassword123",
			name,
			LocalDate.of(1995, 5, 15),
			email,
			Gender.MALE
		);
	}

	public static Member createLocalMemberWithAge(String email, String name, int age) {
		return Member.create(
			"encodedPassword123",
			name,
			LocalDate.now().minusYears(age),
			email,
			Gender.MALE
		);
	}

	public static Member createSocialMember(String email, String name, SocialProvider provider) {
		return Member.createSocialMember(
			email,
			name,
			provider,
			"provider-id-" + email,
			"https://example.com/profile.jpg"
		);
	}

	public static Member createMemberWithId(Long id, String email, String name) {
		Member member = createLocalMember(email, name);
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

	public static Member createMemberWithPassword(String email, String name, String encodedPassword) {
		return Member.create(
			encodedPassword,
			name,
			LocalDate.of(1995, 5, 15),
			email,
			Gender.MALE
		);
	}
}
