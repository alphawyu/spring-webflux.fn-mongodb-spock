package com.realworld.webfluxfn.dto.view;

import com.realworld.webfluxfn.persistence.entity.Comment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Objects;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CommentView {
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private String body;
    private ProfileView author;

    public static CommentView toCommentView(final Comment comment, final ProfileView author) {
        return CommentView.builder()
                .id(comment.getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .body(comment.getBody())
                .author(author).build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommentView that = (CommentView) o;
        return id.equals(that.id)
                && body.equals(that.body)
                && author.equals(that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, body, author);
    }
}
