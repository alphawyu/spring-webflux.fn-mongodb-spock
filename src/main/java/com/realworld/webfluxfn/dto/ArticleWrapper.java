package com.realworld.webfluxfn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.webfluxfn.dto.view.ArticleView;
import com.realworld.webfluxfn.dto.request.CreateArticleRequest;
import com.realworld.webfluxfn.dto.request.UpdateArticleRequest;
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
public class ArticleWrapper<T> {
    @JsonProperty("article")
    private T content;

    @NoArgsConstructor
    public static class ArticleViewWrapper extends ArticleWrapper<ArticleView> {
        public ArticleViewWrapper(final ArticleView article) {
            super(article);
        }
    }

    @NoArgsConstructor
    public static class CreateArticleRequestWrapper extends ArticleWrapper<CreateArticleRequest> {
        public CreateArticleRequestWrapper(final CreateArticleRequest article) {
            super(article);
        }
    }

    @NoArgsConstructor
    public static class UpdateArticleRequestWrapper extends ArticleWrapper<UpdateArticleRequest> {
        public UpdateArticleRequestWrapper(final UpdateArticleRequest article) {
            super(article);
        }
    }
}
