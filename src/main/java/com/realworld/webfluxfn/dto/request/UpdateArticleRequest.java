package com.realworld.webfluxfn.dto.request;

import com.realworld.webfluxfn.validation.NotBlankOrNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateArticleRequest {
    @NotBlankOrNull
    private String title;

    @NotBlankOrNull
    private String description;

    @NotBlankOrNull
    private String body;
}
