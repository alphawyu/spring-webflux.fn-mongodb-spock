package com.realworld.webfluxfn.security;

import com.realworld.webfluxfn.security.JwtProperties;
import com.realworld.webfluxfn.security.JwtSigner;
import com.realworld.webfluxfn.security.TokenExtractor;
import com.realworld.webfluxfn.security.TokenPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    /* default */ ServerAuthenticationConverter jwtServerAuthenticationConverter(final TokenExtractor tokenExtractor) {
        return ex -> Mono.justOrEmpty(ex).flatMap(exchange -> {
            final List<String> headers = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (CollectionUtils.isEmpty(headers)) {
                return Mono.empty();
            }
            final var authHeader = headers.get(0);
            final var token = tokenExtractor.extractToken(authHeader);
            return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
        });
    }

    @Bean
    /* default */ ReactiveAuthenticationManager jwtAuthenticationManager(final JwtSigner tokenService) {
        return authentication -> Mono.justOrEmpty(authentication).map(auth -> {
            final String token = (String) auth.getCredentials();
            final Jws<Claims> jws = tokenService.validate(token);
            final String userId = jws.getBody().getSubject();
            final TokenPrincipal tokenPrincipal = new TokenPrincipal(userId, token);
            return new UsernamePasswordAuthenticationToken(
                    tokenPrincipal,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        });
    }

    @Bean
    /* default */ AuthenticationWebFilter authenticationFilter(final ReactiveAuthenticationManager manager,
                                                               final ServerAuthenticationConverter converter) {
        final AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(manager);
        authenticationWebFilter.setServerAuthenticationConverter(converter);
        return authenticationWebFilter;
    }
}
