package com.realworld.webfluxfn.dto.request;

import com.realworld.webfluxfn.persistence.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindArticlesRequest {
    private int limit = 0;
    private int offset = 20;
    private String authorId = null;
    private String tag = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private User favoritedBy = null;

    public User getFavoritedBy() {
        return this.favoritedBy.toBuilder().build();
    }
    public FindArticlesRequest setFavoritedBy(User favoritedBy) {
        this.favoritedBy = favoritedBy.toBuilder().build();
        return this;
    }
}
