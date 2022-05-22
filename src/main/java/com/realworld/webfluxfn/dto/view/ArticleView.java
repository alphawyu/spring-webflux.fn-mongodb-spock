package com.realworld.webfluxfn.dto.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.persistence.entity.User;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Accessors(chain = true)
@Builder
@Getter
@Setter
@EqualsAndHashCode()
public class ArticleView {
    private String slug;

    private String title;

    private String description;

    private String body;

    @JsonProperty("tagList")
//    @Singular
    private List<String> tags;

    @EqualsAndHashCode.Exclude
    private Instant createdAt;

    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    private Boolean favorited;

    private Integer favoritesCount;

    private ProfileView author;


    public static ArticleView toArticleView(final Article article, final ProfileView author, final boolean favorited) {
        return ArticleView.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .tags(article.getTags())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .favorited(favorited)
                .favoritesCount(article.getFavoritesCount())
                .author(author).build();
    }

    public List<String> getTags() {
        return ImmutableList.copyOf(this.tags);
    }

    public ArticleView setTags(List<String> tags) {
        this.tags = ImmutableList.copyOf(tags);
        return this;
    }

    public static ArticleView toArticleViewForViewer(final Article article, final ProfileView author, final User user) {
        return toArticleView(article, author, article.getFavoritingUserIds().contains(user.getId()));
    }

    public static ArticleView toUnfavoredArticleView(final Article article, final ProfileView author) {
        return toArticleView(article, author, false);
    }
}
