package org.sopt.domain.member.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

	@Test
	@DisplayName("로컬 회원 생성 - create 팩토리 메서드")
	void create_LocalMember_Success() {
		// given
		String password = "encodedPassword";
		String name = "Test User";
		LocalDate birthDate = LocalDate.of(2000, 1, 1);
		String email = "test@example.com";
		Gender gender = Gender.MALE;

		// when
		Member member = Member.create(password, name, birthDate, email, gender);

		// then
		assertThat(member.getPassword()).isEqualTo(password);
		assertThat(member.getName()).isEqualTo(name);
		assertThat(member.getBirthDate()).isEqualTo(birthDate);
		assertThat(member.getEmail()).isEqualTo(email);
		assertThat(member.getGender()).isEqualTo(gender);
		assertThat(member.getProvider()).isEqualTo(SocialProvider.LOCAL);
		assertThat(member.getProviderId()).isNull();
		assertThat(member.getProfileImageUrl()).isNull();
	}

	@Test
	@DisplayName("소셜 회원 생성 - createSocialMember 팩토리 메서드")
	void createSocialMember_Success() {
		// given
		String email = "social@example.com";
		String name = "Social User";
		SocialProvider provider = SocialProvider.GOOGLE;
		String providerId = "google-123";
		String profileImageUrl = "https://example.com/profile.jpg";

		// when
		Member member = Member.createSocialMember(email, name, provider, providerId, profileImageUrl);

		// then
		assertThat(member.getEmail()).isEqualTo(email);
		assertThat(member.getName()).isEqualTo(name);
		assertThat(member.getProvider()).isEqualTo(provider);
		assertThat(member.getProviderId()).isEqualTo(providerId);
		assertThat(member.getProfileImageUrl()).isEqualTo(profileImageUrl);
		assertThat(member.getPassword()).isNull();
		assertThat(member.getBirthDate()).isNull();
		assertThat(member.getGender()).isNull();
	}
}
