package com.realworld.webfluxfn.security;

import com.realworld.webfluxfn.service.user.UserTokenProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtSigner implements UserTokenProvider {

    private final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    private final JwtParser jwtParser = Jwts.parserBuilder()
            .setSigningKey(keyPair.getPublic())
            .build();
    private final JwtProperties jwtProperties;

    public Jws<Claims> validate(final String jwt) {
        return jwtParser.parseClaimsJws(jwt);
    }

    public String generateToken(final String userId) {
        return Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setSubject(userId)
                .setExpiration(expirationDate())
                .compact();
    }

    private Date expirationDate() {
        final var expirationDate = System.currentTimeMillis() + getSessionTime();
        return new Date(expirationDate);
    }

    private long getSessionTime() {
        return jwtProperties.getSessionTime() * 1000L;
    }

    @Override
    public String getToken(final String userId) {
        return generateToken(userId);
    }
}
