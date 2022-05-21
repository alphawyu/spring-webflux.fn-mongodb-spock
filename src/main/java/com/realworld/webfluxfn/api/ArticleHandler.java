package com.realworld.webfluxfn.api;

import com.realworld.webfluxfn.dto.ArticleWrapper.ArticleViewWrapper;
import com.realworld.webfluxfn.dto.ArticleWrapper.CreateArticleRequestWrapper;
import com.realworld.webfluxfn.dto.ArticleWrapper.UpdateArticleRequestWrapper;
import com.realworld.webfluxfn.dto.CommentWrapper.CommentViewWrapper;
import com.realworld.webfluxfn.dto.CommentWrapper.CreateCommentRequestWrapper;
import com.realworld.webfluxfn.dto.view.MultipleArticlesView;
import com.realworld.webfluxfn.dto.view.MultipleCommentsView;
import com.realworld.webfluxfn.dto.view.TagListView;
import com.realworld.webfluxfn.service.article.ArticleService;
import com.realworld.webfluxfn.service.user.UserSessionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleHandler {

    private final ArticleService articleService;
    private final UserSessionProvider userSessionProvider;

    private static final String PATH_VARIABLE_ARTICLE_SLUG = "slug";

    public Mono<ServerResponse> createArticle(final ServerRequest req) {
        final Mono<CreateArticleRequestWrapper> in = req.bodyToMono(CreateArticleRequestWrapper.class);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.createArticle(in.map(e -> e.getContent()), currentUser))
                .map(ArticleViewWrapper::new), ArticleViewWrapper.class);
    }

    public Mono<ServerResponse> getArticles(final ServerRequest req) {
        final int offset = Integer.parseInt(req.queryParam("offset").orElse("0"));
        final int limit = Integer.parseInt(req.queryParam("limit").orElse("20"));
        final String tag = req.queryParam("tag").orElse(null);
        final String favoritedByUser = req.queryParam("favorited").orElse(null);
        final String author = req.queryParam("author").orElse(null);
        return ServerResponse.ok().body( userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.findArticles(
                        tag, author, favoritedByUser, offset, limit, Optional.of(currentUser)))
                .switchIfEmpty(Mono.defer(() -> articleService.findArticles(
                        tag, author, favoritedByUser, offset, limit, Optional.empty()))),
                MultipleArticlesView.class);
    }

    public Mono<ServerResponse> feed(final ServerRequest req) {
        final int offset = Integer.parseInt(req.queryParam("offset").orElse("0"));
        final int limit = Integer.parseInt(req.queryParam("limit").orElse("20"));
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.feed(offset, limit, currentUser)),
                MultipleArticlesView.class);
    }

    public Mono<ServerResponse> getArticle(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.getArticle(slug, Optional.of(currentUser)))
                .switchIfEmpty(Mono.defer(() -> articleService.getArticle(slug, Optional.empty())))
                .map(ArticleViewWrapper::new), ArticleViewWrapper.class);
    }

    public Mono<ServerResponse> updateArticle(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        final Mono<UpdateArticleRequestWrapper> in = req.bodyToMono(UpdateArticleRequestWrapper.class);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.updateArticle(slug, in.map(e -> e.getContent()), currentUser))
                .map(ArticleViewWrapper::new), ArticleViewWrapper.class);
    }

    public Mono<ServerResponse> deleteArticle(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.deleteArticle(slug, currentUser)), Void.class);
    }


    public Mono<ServerResponse> getComments(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.getComments(slug, Optional.of(currentUser)))
                .switchIfEmpty(Mono.defer(() -> articleService.getComments(slug, Optional.empty()))), MultipleCommentsView.class);
    }

    public Mono<ServerResponse> addComment(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        final Mono<CreateCommentRequestWrapper> in = req.bodyToMono(CreateCommentRequestWrapper.class);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.addComment(slug, in.map(e -> e.getContent()), currentUser))
                .map(CommentViewWrapper::new), CommentViewWrapper.class);
    }

    public Mono<ServerResponse> deleteComment(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        final String commentId = req.pathVariable("commentId");
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.deleteComment(commentId, slug, currentUser)), Void.class);
    }

    public Mono<ServerResponse> favoriteArticle(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.favoriteArticle(slug, currentUser))
                .map(ArticleViewWrapper::new), ArticleViewWrapper.class);
    }


    public Mono<ServerResponse> unfavoriteArticle(final ServerRequest req) {
        final String slug = req.pathVariable(PATH_VARIABLE_ARTICLE_SLUG);
        return ServerResponse.ok().body(userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.unfavoriteArticle(slug, currentUser))
                .map(ArticleViewWrapper::new), ArticleViewWrapper.class);
    }

    public Mono<ServerResponse> getTags(final ServerRequest req) {
        return ServerResponse.ok().body(articleService.getTags(), TagListView.class);
    }
}
