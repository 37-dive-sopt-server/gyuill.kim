package org.sopt.domain.member.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.dto.SocialMemberCreateRequest;
import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.domain.service.MemberValidator;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberValidator memberValidator;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private MemberService memberService;

	@Test
	@DisplayName("로컬 회원 생성 성공")
	void create_Success() {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123",
			LocalDate.of(2000, 1, 1),
			"test@example.com",
			Gender.MALE
		);

		String encodedPassword = "encodedPassword123";
		Member member = MemberFixture.createLocalMember(request.email(), request.name());

		given(memberRepository.existsByEmail(request.email())).willReturn(false);
		given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
		given(memberValidator.createValidatedMember(encodedPassword, request.name(), request.birthDate(),
			request.email(), request.gender())).willReturn(member);
		given(memberRepository.save(any(Member.class))).willReturn(member);

		// when
		MemberResponse response = memberService.create(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(request.email());
		assertThat(response.name()).isEqualTo(request.name());

		verify(memberRepository).existsByEmail(request.email());
		verify(passwordEncoder).encode(request.password());
		verify(memberValidator).createValidatedMember(encodedPassword, request.name(), request.birthDate(),
			request.email(), request.gender());
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("로컬 회원 생성 실패 - 이메일 중복 (existsByEmail)")
	void create_DuplicateEmail_ExistsByEmail() {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123",
			LocalDate.of(2000, 1, 1),
			"duplicate@example.com",
			Gender.MALE
		);

		given(memberRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.create(request))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

		verify(memberRepository).existsByEmail(request.email());
		verify(passwordEncoder, never()).encode(anyString());
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("로컬 회원 생성 실패 - 이메일 중복 (DataIntegrityViolationException)")
	void create_DuplicateEmail_DataIntegrityViolation() {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123",
			LocalDate.of(2000, 1, 1),
			"duplicate@example.com",
			Gender.MALE
		);

		String encodedPassword = "encodedPassword123";
		Member member = MemberFixture.createLocalMember(request.email(), request.name());

		given(memberRepository.existsByEmail(request.email())).willReturn(false);
		given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
		given(memberValidator.createValidatedMember(encodedPassword, request.name(), request.birthDate(),
			request.email(), request.gender())).willReturn(member);
		given(memberRepository.save(any(Member.class))).willThrow(DataIntegrityViolationException.class);

		// when & then
		assertThatThrownBy(() -> memberService.create(request))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("ID로 회원 조회 성공")
	void getMemberById_Success() {
		// given
		Long memberId = 1L;
		Member member = MemberFixture.createLocalMember("test@example.com", "Test User");

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

		// when
		MemberResponse response = memberService.getMemberById(memberId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(member.getEmail());
		assertThat(response.name()).isEqualTo(member.getName());

		verify(memberRepository).findById(memberId);
	}

	@Test
	@DisplayName("ID로 회원 조회 실패 - 존재하지 않는 회원")
	void getMemberById_NotFound() {
		// given
		Long memberId = 999L;
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> memberService.getMemberById(memberId))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

		verify(memberRepository).findById(memberId);
	}

	@Test
	@DisplayName("모든 회원 페이징 조회")
	void findAllMembers_Success() {
		// given
		Member member1 = MemberFixture.createLocalMember("user1@example.com", "User1");
		Member member2 = MemberFixture.createLocalMember("user2@example.com", "User2", Gender.FEMALE);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Member> memberPage = new PageImpl<>(List.of(member1, member2), pageable, 2);

		given(memberRepository.findAll(pageable)).willReturn(memberPage);

		// when
		Page<MemberResponse> result = memberService.findAllMembers(pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent().get(0).email()).isEqualTo("user1@example.com");
		assertThat(result.getContent().get(1).email()).isEqualTo("user2@example.com");

		verify(memberRepository).findAll(pageable);
	}

	@Test
	@DisplayName("회원 삭제 성공")
	void deleteMember_Success() {
		// given
		Long memberId = 1L;
		Member member = MemberFixture.createLocalMember("test@example.com", "Test User");

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		willDoNothing().given(memberRepository).delete(member);

		// when
		memberService.deleteMember(memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(memberRepository).delete(member);
	}

	@Test
	@DisplayName("회원 삭제 실패 - 존재하지 않는 회원")
	void deleteMember_NotFound() {
		// given
		Long memberId = 999L;
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> memberService.deleteMember(memberId))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

		verify(memberRepository).findById(memberId);
		verify(memberRepository, never()).delete(any());
	}

	@Test
	@DisplayName("소셜 회원 조회 - 이미 존재하는 회원")
	void getOrCreateSocialMember_ExistingMember() {
		// given
		SocialMemberCreateRequest request = new SocialMemberCreateRequest(
			"social@example.com",
			"Social User",
			SocialProvider.GOOGLE,
			"google-123",
			"https://example.com/profile.jpg"
		);

		Member existingMember = MemberFixture.createSocialMember(request.email(), request.name(),
			request.provider());

		given(memberRepository.findByProviderAndProviderId(request.provider(), request.providerId()))
			.willReturn(Optional.of(existingMember));

		// when
		Member result = memberService.getOrCreateSocialMember(request);

		// then
		assertThat(result).isEqualTo(existingMember);
		assertThat(result.getEmail()).isEqualTo(request.email());

		verify(memberRepository).findByProviderAndProviderId(request.provider(), request.providerId());
		verify(memberRepository, never()).existsByEmail(anyString());
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("소셜 회원 생성 - 새로운 회원")
	void getOrCreateSocialMember_NewMember() {
		// given
		SocialMemberCreateRequest request = new SocialMemberCreateRequest(
			"newsocial@example.com",
			"New Social User",
			SocialProvider.KAKAO,
			"kakao-456",
			null
		);

		Member newMember = MemberFixture.createSocialMember(request.email(), request.name(), request.provider());

		given(memberRepository.findByProviderAndProviderId(request.provider(), request.providerId()))
			.willReturn(Optional.empty());
		given(memberRepository.existsByEmail(request.email())).willReturn(false);
		given(memberRepository.save(any(Member.class))).willReturn(newMember);

		// when
		Member result = memberService.getOrCreateSocialMember(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(request.email());
		assertThat(result.getProvider()).isEqualTo(request.provider());

		verify(memberRepository).findByProviderAndProviderId(request.provider(), request.providerId());
		verify(memberRepository).existsByEmail(request.email());
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("소셜 회원 생성 실패 - 이메일 중복")
	void getOrCreateSocialMember_DuplicateEmail() {
		// given
		SocialMemberCreateRequest request = new SocialMemberCreateRequest(
			"duplicate@example.com",
			"Social User",
			SocialProvider.GOOGLE,
			"google-789",
			null
		);

		given(memberRepository.findByProviderAndProviderId(request.provider(), request.providerId()))
			.willReturn(Optional.empty());
		given(memberRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.getOrCreateSocialMember(request))
			.isInstanceOf(MemberException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

		verify(memberRepository).findByProviderAndProviderId(request.provider(), request.providerId());
		verify(memberRepository).existsByEmail(request.email());
		verify(memberRepository, never()).save(any());
	}
}
