package com.realworld.webfluxfn.dto.request;

import com.google.common.collect.ImmutableList;
import com.realworld.webfluxfn.persistence.entity.Article;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;

//@Data
@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode
public class CreateArticleRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String body;

//    @Singular
    private ImmutableList<String> tagList = ImmutableList.of();

    public Article toArticle(final String id, final String authorId) {
        return Article.builder()
                .id(id)
                .authorId(authorId)
                .description(description)
                .title(title)
                .body(body)
                .tags(Collections.unmodifiableList(tagList))
                .build();
    }

    public CreateArticleRequest setTagList(List<String> tagList) {
        this.tagList = ImmutableList.copyOf(tagList);
        return this;
    }
}
