package com.realworld.webfluxfn.dto.request;

import com.realworld.webfluxfn.persistence.entity.Comment;
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
public class CreateCommentRequest {
    private String body;

    public Comment toComment(final String id, final String authorId) {
        return Comment.builder()
                .id(id)
                .authorId(authorId)
                .body(body)
                .build();
    }
}
