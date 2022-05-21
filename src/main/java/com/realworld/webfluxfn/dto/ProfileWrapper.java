package com.realworld.webfluxfn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.webfluxfn.dto.view.ProfileView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ProfileWrapper {
    @JsonProperty("profile")
    private ProfileView content;
}
