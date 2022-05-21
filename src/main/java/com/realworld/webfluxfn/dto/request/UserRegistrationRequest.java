package com.realworld.webfluxfn.dto.request;

import com.realworld.webfluxfn.persistence.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserRegistrationRequest {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public User toUser(final String encodedPassword, final String id) {
        return User.builder()
                .id(id)
                .encodedPassword(encodedPassword)
                .email(email)
                .username(username)
                .build();
    }

    public User toRawUser() {
        return User.builder()
                .email(email)
                .username(username)
                .build();
    }
}
