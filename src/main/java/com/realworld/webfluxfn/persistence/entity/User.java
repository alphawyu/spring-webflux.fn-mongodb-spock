package com.realworld.webfluxfn.persistence.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Document
@ToString
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    @Setter(AccessLevel.NONE)
    private final String id;

    @Singular
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final List<String> followingIds;

    private String username;

    private String encodedPassword;

    private String email;

    @Nullable
    private String bio;

    @Nullable
    private String image;

    @Builder(toBuilder = true)
    public User(final String id,
                @Nullable final List<String> followingIds,
                final String username,
                final String encodedPassword,
                final String email,
                @Nullable final String bio,
                @Nullable final String image
    ) {
        this.id = id;
        this.followingIds = ofNullable(followingIds).orElse(new ArrayList<>());
        this.username = username;
        this.encodedPassword = encodedPassword;
        this.email = email;
        this.bio = bio;
        this.image = image;
    }

    public List<String> getFollowingIds() {
        return Collections.unmodifiableList(followingIds);
    }

    public void follow(final String userId) {
        followingIds.add(userId);
    }

    public void unfollow(final String userId) {
        followingIds.remove(userId);
    }

    public void follow(final User user) {
        follow(user.getId());
    }

    public void unfollow(final User user) {
        unfollow(user.getId());
    }

    public boolean isFollowing(final User user) {
        return this.followingIds.contains(user.getId());
    }

    public boolean isFollowedBy(final User user) {
        return user.isFollowing(this);
    }
}
