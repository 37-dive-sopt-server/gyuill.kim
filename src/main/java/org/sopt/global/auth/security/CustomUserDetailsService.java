package org.sopt.global.auth.security;

import lombok.RequiredArgsConstructor;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword()
        );
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));

        return new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword()
        );
    }
}
