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

	@Test
	@DisplayName("로컬 회원 vs 소셜 회원 - 필수 필드 차이 검증")
	void localVsSocialMember_RequiredFieldsDifference() {
		// given & when
		Member localMember = Member.create(
			"password123",
			"Local User",
			LocalDate.of(1995, 5, 15),
			"local@example.com",
			Gender.FEMALE
		);
		Member socialMember = Member.createSocialMember(
			"social@example.com",
			"Social User",
			SocialProvider.KAKAO,
			"kakao-456",
			null
		);

		// then - 로컬 회원은 password 필수, provider는 LOCAL
		assertThat(localMember.getPassword()).isEqualTo("password123");
		assertThat(localMember.getProvider()).isEqualTo(SocialProvider.LOCAL);
		assertThat(localMember.getProviderId()).isNull();

		// then - 소셜 회원은 password 없음, providerId 필수
		assertThat(socialMember.getPassword()).isNull();
		assertThat(socialMember.getProvider()).isEqualTo(SocialProvider.KAKAO);
		assertThat(socialMember.getProviderId()).isEqualTo("kakao-456");
	}

	@Test
	@DisplayName("다양한 소셜 Provider 지원")
	void socialMember_VariousProviders() {
		// given & when
		Member googleMember = Member.createSocialMember(
			"google@example.com",
			"Google User",
			SocialProvider.GOOGLE,
			"google-789",
			"https://google.com/profile.jpg"
		);
		Member kakaoMember = Member.createSocialMember(
			"kakao@example.com",
			"Kakao User",
			SocialProvider.KAKAO,
			"kakao-101",
			"https://kakao.com/profile.jpg"
		);

		// then - 각 Provider가 올바르게 설정됨
		assertThat(googleMember.getProvider()).isEqualTo(SocialProvider.GOOGLE);
		assertThat(googleMember.getProviderId()).isEqualTo("google-789");

		assertThat(kakaoMember.getProvider()).isEqualTo(SocialProvider.KAKAO);
		assertThat(kakaoMember.getProviderId()).isEqualTo("kakao-101");
	}

	@Test
	@DisplayName("소셜 회원 프로필 이미지 URL 없이 생성 가능")
	void socialMember_WithoutProfileImage() {
		// given & when
		Member member = Member.createSocialMember(
			"noprofile@example.com",
			"No Profile User",
			SocialProvider.GOOGLE,
			"google-999",
			null
		);

		// then
		assertThat(member.getProfileImageUrl()).isNull();
	}

	@Test
	@DisplayName("로컬 회원 - 모든 Gender 타입")
	void localMember_AllGenderTypes() {
		// given & when
		Member maleMember = Member.create("pw", "Male User", LocalDate.now().minusYears(25),
			"male@example.com", Gender.MALE);
		Member femaleMember = Member.create("pw", "Female User", LocalDate.now().minusYears(25),
			"female@example.com", Gender.FEMALE);

		// then
		assertThat(maleMember.getGender()).isEqualTo(Gender.MALE);
		assertThat(femaleMember.getGender()).isEqualTo(Gender.FEMALE);
	}
}
