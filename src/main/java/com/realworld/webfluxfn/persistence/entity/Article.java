package com.realworld.webfluxfn.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Document
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Article {

    public static final String CREATED_AT_FIELD_NAME = "createdAt";
    public static final String FAVORITING_USER_IDS = "favoritingUserIds";
    public static final String AUTHOR_ID_FIELD_NAME = "authorId";
    public static final String TAGS_FIELD_NAME = "tags";

    @Getter
    @EqualsAndHashCode.Include
    private final String id;

    @Getter
    private final Instant createdAt;

    @Getter
    @LastModifiedDate
    private final Instant updatedAt;

    @Getter
    private final List<String> tags;

    @Getter
    private final List<Comment> comments;

    @Getter
    private final List<String> favoritingUserIds;

    @Getter
    private String slug;

    @Getter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String body;

    @Getter
    @Setter
    private String authorId;

    @Builder
    /* default */ Article(final String id,
            final String title,
            final String description,
            final String body,
            @Nullable final Instant createdAt,
            @Nullable final Instant updatedAt,
            final String authorId,
            @Nullable final List<String> tags,
            @Nullable final List<Comment> comments,
            @Nullable final List<String> favoritingUserIds
    ) {
        this.id = id;
        this.title = title;
        this.slug = toSlug(title);
        this.description = description;
        this.body = body;
        this.createdAt = ofNullable(createdAt).orElse(Instant.now());
        this.updatedAt = ofNullable(updatedAt).orElse(createdAt);
        this.authorId = authorId;
        this.tags = ofNullable(tags).orElse(new ArrayList<>());
        this.comments = ofNullable(comments).orElse(new ArrayList<>());
        this.favoritingUserIds = ofNullable(favoritingUserIds).orElse(new ArrayList<>());
    }

    public int getFavoritesCount() {
        return favoritingUserIds == null ? 0 : favoritingUserIds.size();
    }
    public Article addComment(final Comment comment) {
        this.comments.add(comment);
        return this;
    }

    public Article deleteComment(final Comment comment) {
        this.comments.remove(comment);
        return this;
    }

    public void setTitle(final String title) {
        this.title = title;
        this.slug = toSlug(title);
    }

    public Optional<Comment> getCommentById(final String commentId) {
        return comments.stream()
                .filter(comment -> commentId.equals(comment.getId()))
                .findFirst();
    }

    public boolean hasTag(final String tag) {
        return tags.contains(tag);
    }

    public boolean isAuthor(final  String authorId) {
        return this.authorId.equals(authorId);
    }

    public boolean isAuthor(final User author) {
        return isAuthor(author.getId());
    }

    public boolean favoriteByUser(final User user) {
        if (favoritingUserIds.contains(user.getId())) {
            return false;
        }
        favoritingUserIds.add(user.getId());
        return true;
    }

    public boolean unfavoriteByUser(final User user) {
        if (! favoritingUserIds.contains(user.getId())) {
            return false;
        }
        favoritingUserIds.remove(user.getId());
        return true;
    }

    private static String toSlug(final String title) {
        return title.toLowerCase(Locale.US).replaceAll("[&|\\uFE30-\\uFFA0’”\\s?,.]+", "-");
    }
}