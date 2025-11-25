package org.sopt.domain.member.domain.repository;

import java.util.Optional;

import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(String email);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);

	boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
}
