package org.sopt.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(jwtProperties.getExpiresInSeconds());

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("email", email)
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
        } catch (TokenExpiredException e) {
            return false;
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

    public String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                .build()
                .verify(token);
        return decodedJWT.getClaim("email").asString();
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
