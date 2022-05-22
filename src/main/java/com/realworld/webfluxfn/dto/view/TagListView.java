package com.realworld.webfluxfn.dto.view;

import com.google.common.collect.ImmutableList;
import com.realworld.webfluxfn.persistence.entity.Tag;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

//@Data
@Accessors(chain = true)
//@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class TagListView {
    private List<String> tags;

    public static TagListView makeInstance(final List<Tag> tags) {
        final var rowTags = tags.stream().map(Tag::getTagName).toList();
        return new TagListView()
                .setTags(rowTags);
    }

    public List<String> getTags() {
        return ImmutableList.copyOf(tags);
    }

    public TagListView setTags(List<String> tags) {
        this.tags = ImmutableList.copyOf(tags);
        return this;
    }
}
