package org.sopt.member.domain.repository;

import java.util.Optional;
import org.sopt.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
