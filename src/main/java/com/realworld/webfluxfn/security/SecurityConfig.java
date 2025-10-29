package com.realworld.webfluxfn.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    /* default */ SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http,
            final AuthenticationWebFilter webFilter,
            final Customizer<AuthorizeExchangeSpec> endpointsConfig) {
        return http.authorizeExchange(endpointsConfig)
                .addFilterAt(webFilter, SecurityWebFiltersOrder.AUTHENTICATION)
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
    /* default */ Customizer<AuthorizeExchangeSpec>  endpointsConfig() {
        return http -> http
                .pathMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/profiles/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/tags/**").permitAll()
                .anyExchange().authenticated();
    }
 
}
