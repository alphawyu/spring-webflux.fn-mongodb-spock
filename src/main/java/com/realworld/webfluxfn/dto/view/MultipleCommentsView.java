package com.realworld.webfluxfn.dto.view;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

//@Data
@Accessors(chain = true)
//@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class MultipleCommentsView {
    private List<CommentView> comments = Collections.emptyList();

    public static MultipleCommentsView makeInstance(final List<CommentView> comments) {
        return new MultipleCommentsView()
                .setComments(comments);
    }

    public List<CommentView> getComments() {
        return ImmutableList.copyOf(comments);
    }

    public MultipleCommentsView setComments(List<CommentView> comments) {
        this.comments = ImmutableList.copyOf(comments);
        return this;
    }
}
