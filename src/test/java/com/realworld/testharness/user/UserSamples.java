package com.realworld.testharness.user;

import com.realworld.webfluxfn.dto.request.UpdateUserRequest;
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest;
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.service.user.PasswordService;

import java.util.ArrayList;
import java.util.UUID;

public class UserSamples {

    public static final String SAMPLE_USERNAME = "Test username";
    public static final String SAMPLE_EMAIL = "testemail@gmail.com";
    public static final String SAMPLE_PASSWORD = "testpassword";
    public static final String SAMPLE_USER_ID = UUID.randomUUID().toString();
    private static final PasswordService passwordService = new PasswordService();

    public static UserRegistrationRequest sampleUserRegistrationRequest() {
        return UserRegistrationRequest.builder()
                .username(SAMPLE_USERNAME)
                .email(SAMPLE_EMAIL)
                .password(SAMPLE_PASSWORD).build();
    }

    public static UserAuthenticationRequest sampleUserAuthenticationRequest() {
        return UserAuthenticationRequest.builder()
                .email(SAMPLE_EMAIL)
                .password(SAMPLE_PASSWORD).build();
    }

    public static User.UserBuilder sampleUser(PasswordService passwordService) {
        var encodePassword = passwordService.encodePassword(SAMPLE_PASSWORD);
        return User.builder()
                .id(SAMPLE_USER_ID)
                .username(SAMPLE_USERNAME)
                .email(SAMPLE_EMAIL)
                .encodedPassword(encodePassword)
                .image("test image url")
                .bio("test bio")
                .followingIds(new ArrayList<>());
    }

    public static User.UserBuilder sampleUser() {
        return sampleUser(passwordService);
    }

    public static UpdateUserRequest sampleUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .bio("new bio")
                .email("newemail@gmail.com")
                .image("new image")
                .username("new username")
                .password("new password").build();
    }
}
