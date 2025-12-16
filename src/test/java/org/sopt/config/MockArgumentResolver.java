package org.sopt.config;

import org.sopt.global.auth.security.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Controller 테스트에서 @AuthenticationPrincipal CustomUserDetails를 모킹하기 위한 ArgumentResolver
 * Spring Security를 제외하면서도 인증이 필요한 엔드포인트를 테스트할 수 있게 해줍니다.
 */
public class MockArgumentResolver implements HandlerMethodArgumentResolver {

	private final Long memberId;
	private final String email;
	private final String password;

	public MockArgumentResolver() {
		this(1L, "test@example.com", "password");
	}

	public MockArgumentResolver(Long memberId, String email, String password) {
		this.memberId = memberId;
		this.email = email;
		this.password = password;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
			&& parameter.getParameterType().equals(CustomUserDetails.class);
	}

	@Override
	public Object resolveArgument(
		@Nullable MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		@Nullable NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		return new CustomUserDetails(memberId, email, password);
	}
}
