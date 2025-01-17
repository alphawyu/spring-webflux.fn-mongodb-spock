package com.realworld.webfluxfn.security;

import com.realworld.testharness.ImportAppSecurity;
import com.realworld.testharness.TokenHelper;
import com.realworld.webfluxfn.exception.GlobalErrorAttributes;
import com.realworld.webfluxfn.exception.GlobalErrorWebExceptionHandler;
import com.realworld.webfluxfn.service.user.UserTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = {SecurityTest.Controller.class})
@Import({GlobalErrorWebExceptionHandler.class, GlobalErrorAttributes.class})
@ImportAppSecurity
class SecurityTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserTokenProvider tokenProvider;

    @Test
    void shouldReturn201() {
        var status = webTestClient.get()
                .uri("/permitAll")
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturn401() {
        var status = webTestClient.get()
                .uri("/authenticated")
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getStatus();
        assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUserId() {
        var userId = "1";
        var token = tokenProvider.getToken(userId);
        var result = webTestClient.get()
                .uri("/authenticated")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(TokenPrincipal.class)
                .returnResult();
        var status = result.getStatus();
        var body = Objects.requireNonNull(result.getResponseBody());
        assertThat(status).isEqualTo(HttpStatus.OK);
        assertThat(body.userId()).isEqualTo(userId);
        assertThat(body.token()).isEqualTo(token);
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        Controller testController() {
            return new Controller();
        }

        @Bean
        @Primary
        SecurityConfig.EndpointsSecurityConfig testEndpointsConfig() {
            return http -> http
                    .pathMatchers("/permitAll").permitAll()
                    .pathMatchers("/authenticated").authenticated();
        }
    }

    @RestController
    static class Controller {
        @GetMapping("/authenticated")
        public Mono<TokenPrincipal> token(@AuthenticationPrincipal Mono<TokenPrincipal> principalMono) {
            return principalMono;
        }

        @GetMapping("/permitAll")
        public void free() {
        }
    }
}
