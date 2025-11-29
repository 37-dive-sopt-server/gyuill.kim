package org.sopt.global.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

	private String secret;
	private Long expiresInSeconds;
	private Long refreshExpiresInSeconds;
}
