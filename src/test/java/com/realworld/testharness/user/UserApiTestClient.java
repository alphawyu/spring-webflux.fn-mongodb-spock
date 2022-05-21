package com.realworld.testharness.user;

import com.realworld.testharness.TokenHelper;
import com.realworld.webfluxfn.dto.ProfileWrapper;
import com.realworld.webfluxfn.dto.UserWrapper;
import com.realworld.webfluxfn.dto.request.UpdateUserRequest;
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest;
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest;
import com.realworld.webfluxfn.dto.view.ProfileView;
import com.realworld.webfluxfn.dto.view.UserView;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

public class UserApiTestClient {
    private final WebTestClient webTestClient;
    private static final String API_PREFIX = "/api";

    public UserApiTestClient(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    public UserView updateUser(String token, UpdateUserRequest updateUserRequest) {
        var result = webTestClient.put()
                .uri(API_PREFIX + "/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .bodyValue(new UserWrapper.UpdateUserRequestWrapper(updateUserRequest))
                .exchange()
                .expectBody(UserWrapper.UserViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public UserView currentUser(String token) {
        var response = webTestClient.get()
                .uri(API_PREFIX + "/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(UserWrapper.UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public UserView signup(UserRegistrationRequest userRegistrationRequest) {
        var response = webTestClient.post()
                .uri(API_PREFIX + "/users")
                .bodyValue(new UserWrapper.UserRegistrationRequestWrapper(userRegistrationRequest))
                .exchange()
                .expectBody(UserWrapper.UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public UserView signup() {
        var user = signup(UserSamples.sampleUserRegistrationRequest());
        assert user != null;
        return user;
    }

    public UserView login(UserAuthenticationRequest userAuthenticationRequest) {
        var response = webTestClient.post()
                .uri(API_PREFIX + "/users/login")
                .bodyValue(new UserWrapper.UserAuthenticationRequestWrapper(userAuthenticationRequest))
                .exchange()
                .expectBody(UserWrapper.UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public ProfileView getProfile(String username) {
        var result = webTestClient.get()
                .uri(API_PREFIX + "/profiles/" + username)
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView getProfile(String username, String token) {
        var result = webTestClient.get()
                .uri(API_PREFIX + "/profiles/" + username)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView follow(String username, String token) {
        var result = webTestClient.post()
                .uri(API_PREFIX + "/profiles/" + username + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView unfollow(String followeeUsername, String authToken) {
        var result = webTestClient.delete()
                .uri(API_PREFIX + "/profiles/" + followeeUsername + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }
}
