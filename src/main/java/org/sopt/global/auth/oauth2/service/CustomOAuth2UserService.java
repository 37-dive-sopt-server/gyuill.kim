package org.sopt.global.auth.oauth2.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.domain.member.application.service.MemberService;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.entity.SocialProvider;
import org.sopt.global.auth.oauth2.dto.OAuth2UserInfo;
import org.sopt.global.auth.oauth2.strategy.OAuth2UserInfoStrategy;
import org.sopt.global.auth.oauth2.strategy.OAuth2UserInfoStrategyFactory;
import org.sopt.global.auth.security.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final OAuth2UserInfoStrategyFactory strategyFactory;
	private final MemberService memberService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		Map<String, Object> attributes = oauth2User.getAttributes();

		OAuth2UserInfoStrategy strategy = strategyFactory.getStrategy(registrationId);
		OAuth2UserInfo userInfo = strategy.extractUserInfo(attributes);

		SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

		Member member = memberService.getOrCreateSocialMember(
			userInfo.getEmail(),
			userInfo.getName(),
			provider,
			userInfo.getProviderId(),
			userInfo.getProfileImageUrl()
		);

		return new CustomUserDetails(member.getId(), member.getEmail(), member.getPassword());
	}
}
