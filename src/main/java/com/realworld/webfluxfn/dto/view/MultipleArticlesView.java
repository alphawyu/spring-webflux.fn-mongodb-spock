package com.realworld.webfluxfn.dto.view;

import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

//@Data
@Accessors(chain = true)
//@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class MultipleArticlesView {

    private List<ArticleView> articles = Collections.emptyList();

    @Getter
    @Setter
    private int articlesCount;

    public static MultipleArticlesView makeInstance(final List<ArticleView> articles) {
        return new MultipleArticlesView()
                .setArticles(articles)
                .setArticlesCount(articles.size());
    }

    public List<ArticleView> getArticles() {
        return ImmutableList.copyOf(articles);
    }

    public MultipleArticlesView setArticles(List<ArticleView> articles) {
        this.articles = ImmutableList.copyOf(articles);
        return this;
    }
}
