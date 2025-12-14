package org.sopt.domain.member.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.annotation.RepositoryTest;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.sopt.fixture.MemberFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@RepositoryTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("이메일로 회원 조회 성공")
	void findByEmail_Success() {
		// given
		Member member = MemberFixture.createLocalMember("test@example.com", "Test User");
		memberRepository.save(member);
		entityManager.clear();

		// when
		Optional<Member> result = memberRepository.findByEmail("test@example.com");

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("test@example.com");
		assertThat(result.get().getName()).isEqualTo("Test User");
	}

	@Test
	@DisplayName("존재하지 않는 이메일 조회 시 빈 Optional 반환")
	void findByEmail_NotFound() {
		// when
		Optional<Member> result = memberRepository.findByEmail("nonexistent@example.com");

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("이메일 중복 확인 - 존재하는 경우 true 반환")
	void existsByEmail_True() {
		// given
		Member member = MemberFixture.createLocalMember("exists@example.com", "Existing User");
		memberRepository.save(member);
		entityManager.clear();

		// when
		boolean exists = memberRepository.existsByEmail("exists@example.com");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("이메일 중복 확인 - 존재하지 않는 경우 false 반환")
	void existsByEmail_False() {
		// when
		boolean exists = memberRepository.existsByEmail("nonexistent@example.com");

		// then
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("Provider와 ProviderId로 소셜 회원 조회 성공")
	void findByProviderAndProviderId_Success() {
		// given
		Member socialMember = MemberFixture.createSocialMember("social@example.com", "Social User",
			SocialProvider.GOOGLE);
		memberRepository.save(socialMember);
		entityManager.clear();

		// when
		Optional<Member> result = memberRepository.findByProviderAndProviderId(
			SocialProvider.GOOGLE, "provider-id-social@example.com"
		);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getProvider()).isEqualTo(SocialProvider.GOOGLE);
		assertThat(result.get().getEmail()).isEqualTo("social@example.com");
	}

	@Test
	@DisplayName("존재하지 않는 Provider와 ProviderId 조회 시 빈 Optional 반환")
	void findByProviderAndProviderId_NotFound() {
		// when
		Optional<Member> result = memberRepository.findByProviderAndProviderId(
			SocialProvider.KAKAO, "nonexistent-provider-id"
		);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("이메일 unique constraint 위반 시 예외 발생")
	void saveWithDuplicateEmail_ThrowsException() {
		// given
		Member member1 = MemberFixture.createLocalMember("duplicate@example.com", "User 1");
		memberRepository.save(member1);
		entityManager.flush();
		entityManager.clear();

		Member member2 = MemberFixture.createLocalMember("duplicate@example.com", "User 2");

		// when & then
		assertThatThrownBy(() -> {
			memberRepository.save(member2);
			entityManager.flush();  // flush to trigger constraint check
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("회원 저장 및 ID 자동 생성 확인")
	void save_GeneratesId() {
		// given
		Member member = MemberFixture.createLocalMember("newuser@example.com", "New User");

		// when
		Member saved = memberRepository.save(member);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getEmail()).isEqualTo("newuser@example.com");
	}

	@Test
	@DisplayName("회원 삭제")
	void delete_Success() {
		// given
		Member member = MemberFixture.createLocalMember("delete@example.com", "Delete User");
		Member saved = memberRepository.save(member);
		Long memberId = saved.getId();
		entityManager.clear();

		// when
		memberRepository.deleteById(memberId);

		// then
		Optional<Member> result = memberRepository.findById(memberId);
		assertThat(result).isEmpty();
	}
}
