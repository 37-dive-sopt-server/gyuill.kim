package org.sopt.global.auth.jwt;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(jwtProperties.getExpiresInSeconds());

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiryDate))
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(jwtProperties.getRefreshExpiresInSeconds());

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiryDate))
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                .build()
                .verify(token);
        return Long.parseLong(decodedJWT.getSubject());
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

