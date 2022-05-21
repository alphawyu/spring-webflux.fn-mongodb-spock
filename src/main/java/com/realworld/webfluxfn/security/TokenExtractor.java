package com.realworld.webfluxfn.security;

import com.realworld.webfluxfn.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {
    public String extractToken(final String authorizationHeader) {
        if (!authorizationHeader.startsWith("Token ")) {
            throw new InvalidRequestException("Authorization Header", "has no `Token` prefix");
        }
        return authorizationHeader.substring("Token ".length());
    }
}
