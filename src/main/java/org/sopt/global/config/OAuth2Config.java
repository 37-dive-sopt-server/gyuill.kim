package org.sopt.global.config;

import org.sopt.global.auth.oauth2.handler.OAuth2AuthenticationFailureHandler;
import org.sopt.global.auth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import org.sopt.global.auth.oauth2.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler successHandler;
	private final OAuth2AuthenticationFailureHandler failureHandler;

	@Bean
	public Customizer<OAuth2LoginConfigurer<HttpSecurity>> oauth2LoginCustomizer() {
		return oauth2 -> oauth2
			.userInfoEndpoint(userInfo -> userInfo
				.userService(customOAuth2UserService)
			)
			.successHandler(successHandler)
			.failureHandler(failureHandler);
	}
}
