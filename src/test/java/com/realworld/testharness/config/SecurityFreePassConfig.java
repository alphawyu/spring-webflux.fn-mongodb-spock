package com.realworld.testharness.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@TestConfiguration
@EnableWebFluxSecurity
public class SecurityFreePassConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationWebFilter webFilter, EndpointsSecurityConfig endpointsConfig) {
        var authorizeExchange = http.authorizeExchange();
        return endpointsConfig.apply(authorizeExchange)
                .and()
//                .addFilterAt(webFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic().disable()
                .cors().disable()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .build();
    }

    /**
     * Moving endpoints config to particular interface allow to change endpoints in tests.
     */
    @Bean
    EndpointsSecurityConfig endpointsConfig() {
        return http -> http
                .pathMatchers(HttpMethod.POST, "/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/**").permitAll()
                .pathMatchers(HttpMethod.PUT, "/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/**").permitAll()
                .anyExchange().authenticated();
    }

    @FunctionalInterface
    public interface EndpointsSecurityConfig {
        AuthorizeExchangeSpec apply(AuthorizeExchangeSpec http);
    }
}
