package com.realworld.webfluxfn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.webfluxfn.dto.view.CommentView;
import com.realworld.webfluxfn.dto.request.CreateCommentRequest;
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
public class CommentWrapper<T> {
    @JsonProperty("comment")
    private T content;


    @NoArgsConstructor
    public static class CreateCommentRequestWrapper extends CommentWrapper<CreateCommentRequest> {
        public CreateCommentRequestWrapper(final CreateCommentRequest comment) {
            super(comment);
        }
    }

    @NoArgsConstructor
    public static class CommentViewWrapper extends CommentWrapper<CommentView> {
        public CommentViewWrapper(final CommentView comment) {
            super(comment);
        }
    }
}
