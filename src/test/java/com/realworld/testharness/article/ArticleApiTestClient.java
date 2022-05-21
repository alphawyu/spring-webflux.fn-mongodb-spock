package com.realworld.testharness.article;

import com.realworld.testharness.TokenHelper;
import com.realworld.webfluxfn.dto.ArticleWrapper;
import com.realworld.webfluxfn.dto.CommentWrapper;
import com.realworld.webfluxfn.dto.view.*;
import com.realworld.webfluxfn.dto.request.CreateArticleRequest;
import com.realworld.webfluxfn.dto.request.CreateCommentRequest;
import com.realworld.webfluxfn.dto.request.UpdateArticleRequest;
import com.realworld.webfluxfn.dto.view.UserView;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

public class ArticleApiTestClient {
    private final WebTestClient webTestClient;
    private static final String API_PREFIX = "/api";

    public ArticleApiTestClient(WebTestClient client) {
        this.webTestClient = client;
    }

    public ArticleView createArticle(CreateArticleRequest createArticleRequest, String authToken) {
        var result = webTestClient.post()
                .uri(API_PREFIX + "/articles")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(new ArticleWrapper.CreateArticleRequestWrapper(createArticleRequest))
                .exchange()
                .expectBody(ArticleWrapper.ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public EntityExchangeResult<MultipleArticlesView> findArticles(FindArticlesRequest request, String authToken) {
        var requestSpec = webTestClient
                .get()
                .uri(builder -> builder
                        .path(API_PREFIX + "/articles")
                        .queryParamIfPresent("tag", ofNullable(request.getTag()))
                        .queryParamIfPresent("author", ofNullable(request.getAuthor()))
                        .queryParamIfPresent("favorited", ofNullable(request.getFavorited()))
                        .build()
                );
        if (authToken != null) {
            requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken));
        }
        return requestSpec
                .exchange()
                .expectBody(MultipleArticlesView.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleArticlesView> feed(String authToken, Integer offset, Integer limit) {
        return webTestClient.get()
                .uri(builder -> builder.path(API_PREFIX + "/articles/feed")
                        .queryParamIfPresent("limit", ofNullable(limit))
                        .queryParamIfPresent("offset", ofNullable(offset))
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(MultipleArticlesView.class)
                .returnResult();
    }

    public ArticleView getArticle(String slug, String authToken) {
        var result = webTestClient.get()
                .uri(API_PREFIX + "/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ArticleWrapper.ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public EntityExchangeResult<MultipleArticlesView> findArticles(FindArticlesRequest request) {
        return findArticles(request, null);
    }

    public ArticleView updateArticle(String slug, UpdateArticleRequest updateArticleRequest, String authToken) {
        var result = webTestClient.put()
                .uri(API_PREFIX + "/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(new ArticleWrapper.UpdateArticleRequestWrapper(updateArticleRequest))
                .exchange()
                .expectBody(ArticleWrapper.ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public CommentView addComment(String articleSlug, CreateCommentRequest request, String authToken) {
        var result = webTestClient.post()
                .uri(API_PREFIX + "/articles/" + articleSlug + "/comments")
                .bodyValue(new CommentWrapper.CreateCommentRequestWrapper(request))
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(CommentWrapper.CommentViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public CommentView addComment(String articleSlug, String body, String authToken) {
        return addComment(articleSlug, new CreateCommentRequest(body), authToken);
    }

    public EntityExchangeResult<Void> deleteComment(String articleSlug, String commentId, String authToken) {
        return webTestClient.delete()
                .uri(API_PREFIX + "/articles/" + articleSlug + "/comments/" + commentId)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(Void.class)
                .returnResult();
    }

    public EntityExchangeResult<Void> deleteArticle(String slug, String authToken) {
        return webTestClient.delete()
                .uri(API_PREFIX + "/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(Void.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleCommentsView> getComments(String articleSlug, String authToken) {
        return webTestClient.get()
                .uri(API_PREFIX + "/articles/" + articleSlug + "/comments")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(MultipleCommentsView.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleCommentsView> getComments(String articleSlug) {
        return webTestClient.get()
                .uri(API_PREFIX + "/articles/" + articleSlug + "/comments")
                .exchange()
                .expectBody(MultipleCommentsView.class)
                .returnResult();
    }

    public ArticleView favoriteArticle(String articleSlug, UserView user) {
        var result = webTestClient.post()
                .uri(API_PREFIX + "/articles/{slug}/favorite", articleSlug)
                .headers(authHeader(user))
                .exchange()
                .expectBody(ArticleWrapper.ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ArticleView unfavoriteArticle(String articleSlug, UserView user) {
        var result = webTestClient.delete()
                .uri(API_PREFIX + "/articles/{slug}/favorite", articleSlug)
                .headers(authHeader(user))
                .exchange()
                .expectBody(ArticleWrapper.ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }


    public EntityExchangeResult<TagListView> getTags() {
        return webTestClient.get()
                .uri(API_PREFIX + "/tags")
                .exchange()
                .expectBody(TagListView.class)
                .returnResult();
    }

    private Consumer<HttpHeaders> authHeader(UserView user) {
        return headers -> headers.put(HttpHeaders.AUTHORIZATION, List.of(TokenHelper.formatToken(user.getToken())));
    }
}
