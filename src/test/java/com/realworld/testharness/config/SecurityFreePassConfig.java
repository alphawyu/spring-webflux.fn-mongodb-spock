package com.realworld.testharness.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@TestConfiguration
@EnableWebFluxSecurity
public class SecurityFreePassConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AuthenticationWebFilter webFilter,
            Customizer<AuthorizeExchangeSpec> endpointsConfig) {
        return http.authorizeExchange(endpointsConfig)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }

    /** 
     * Moving endpoints config to particular interface allow to change endpoints in
     * tests.
     */
    @Bean
    Customizer<AuthorizeExchangeSpec> endpointsConfig() {
        return http -> http
                .pathMatchers(HttpMethod.POST, "/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/**").permitAll()
                .pathMatchers(HttpMethod.PUT, "/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/**").permitAll()
                .anyExchange().authenticated();
    }

}
