package org.sopt.global.config;

import org.sopt.global.auth.exception.JwtAuthenticationEntryPoint;
import org.sopt.global.auth.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final Customizer<OAuth2LoginConfigurer<HttpSecurity>> oauth2LoginCustomizer;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/members").permitAll()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
				.requestMatchers("/members/**", "/articles/**").authenticated()
				.anyRequest().denyAll()
			)
			.oauth2Login(oauth2LoginCustomizer)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
