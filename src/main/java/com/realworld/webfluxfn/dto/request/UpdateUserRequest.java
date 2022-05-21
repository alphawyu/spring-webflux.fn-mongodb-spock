package com.realworld.webfluxfn.dto.request;

import com.realworld.webfluxfn.validation.NotBlankOrNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateUserRequest {
    @Email
    @NotBlankOrNull
    private String email;

    @NotBlankOrNull
    private String username;

    @NotBlankOrNull
    private String password;

    private String image;

    private String bio;
}
