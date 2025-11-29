package org.sopt.domain.member.domain.entity;

import java.time.LocalDate;

import org.sopt.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", indexes = {
	@Index(name = "idx_member_name", columnList = "name"),
	@Index(name = "idx_member_provider_providerid", columnList = "provider, providerId")
})
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column
	private LocalDate birthDate;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column
	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private SocialProvider provider;

	@Column(length = 255)
	private String providerId;

	@Column(length = 500)
	private String profileImageUrl;

	private Member(String password, String name, LocalDate birthDate, String email, Gender gender, SocialProvider provider, String providerId, String profileImageUrl) {
		this.password = password;
		this.name = name;
		this.birthDate = birthDate;
		this.email = email;
		this.gender = gender;
		this.provider = provider;
		this.providerId = providerId;
		this.profileImageUrl = profileImageUrl;
	}

	public static Member create(String password, String name, LocalDate birthDate, String email, Gender gender) {
		return new Member(password, name, birthDate, email, gender, SocialProvider.LOCAL, null, null);
	}

	public static Member createSocialMember(String email, String name, SocialProvider provider, String providerId, String profileImageUrl) {
		return new Member(null, name, null, email, null, provider, providerId, profileImageUrl);
	}
}
