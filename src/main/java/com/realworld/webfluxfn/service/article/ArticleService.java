package com.realworld.webfluxfn.service.article;

import com.realworld.webfluxfn.dto.view.*;
import com.realworld.webfluxfn.dto.request.CreateArticleRequest;
import com.realworld.webfluxfn.dto.request.CreateCommentRequest;
import com.realworld.webfluxfn.dto.request.UpdateArticleRequest;
import com.realworld.webfluxfn.dto.view.ProfileView;
import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.persistence.repository.ArticleRepository;
import com.realworld.webfluxfn.persistence.repository.TagRepository;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserArticleService userArticleService;

    public Mono<TagListView> getTags() {
        return this.tagRepository.findAll()
                .collectList()
                .map(TagListView::makeInstance);
    }

    public Mono<ArticleView> createArticle(final Mono<CreateArticleRequest> request, final User author) {
        final String id = UUID.randomUUID().toString();
        final ProfileView profileView = ProfileView.toUnfollowedProfileView(author);
        final Mono<Article> newArticle = request.map(e -> e.toArticle(id, author.getId()));
        return Mono.defer(() -> this.articleRepository.saveAll(newArticle).single()
                .flatMap(article -> Mono.defer(() -> this.tagRepository
                        .saveAllTags(article.getTags()).then(Mono.just(article))))
                .map(article -> ArticleView.toUnfavoredArticleView(article, profileView)));
    }

    public Mono<MultipleArticlesView> feed(final int offset, final int limit, final User currentUser) {
        final var followingAuthorIds = currentUser.getFollowingIds();
        return Mono.defer(() -> this.articleRepository
                .findNewestArticlesByAuthorIds(followingAuthorIds, offset, limit)
                .flatMap(article -> Mono.defer(() -> this.userArticleService.mapToArticleView(article, currentUser)))
                .collectList()
                .map(MultipleArticlesView::makeInstance));
    }

    public Mono<MultipleArticlesView> findArticles(final String tag, final String authorName, final String favoritingUserName,
                                                   final int offset, final int limit, final Optional<User> currentUser) {
        return this.userArticleService.findArticles(tag, authorName, favoritingUserName, offset, limit, currentUser);
    }

    public Mono<ArticleView> getArticle(final String slug, final Optional<User> currentUser) {
        return Mono.defer(() -> this.articleRepository.findBySlug(slug)
                .flatMap(article -> this.userArticleService.mapToArticleView(article, currentUser)));
    }

    public Mono<ArticleView> updateArticle(final String slug, final Mono<UpdateArticleRequest> request, final User actionUser) {
        return this.articleRepository.findBySlugOrFail(slug)
                .flatMap(article -> {
                    if (!article.isAuthor(actionUser)) {
                        return Mono.error(new InvalidRequestException("Article", "only author can update article"));
                    }
                    return request.map(m -> {
                        ofNullable(m.getBody()).ifPresent(article::setBody);
                        ofNullable(m.getDescription()).ifPresent(article::setDescription);
                        ofNullable(m.getTitle()).ifPresent(article::setTitle);
                        return article;
                    });
                })
                .flatMap(r -> this.articleRepository.save(r) )
                .flatMap(article -> this.userArticleService.mapToArticleView(article, actionUser));
    }

    public Mono<Void> deleteArticle(final String slug, final User articleAuthor) {
        return this.articleRepository.findBySlug(slug)
                .flatMap(article -> {
                    if (!article.isAuthor(articleAuthor)) {
                        return Mono.error(new InvalidRequestException("Article", "only author can delete article"));
                    }
                    return this.articleRepository.deleteArticleBySlug(slug).then();
                });
    }

    public Mono<CommentView> addComment(final String slug, final Mono<CreateCommentRequest> request, final User currentUser) {
        return this.userArticleService.addComment(slug, request, currentUser);
    }

    public Mono<Void> deleteComment(final String commentId, final String slug, final User user) {
        return this.userArticleService.deleteComment(commentId, slug, user);
    }

    public Mono<MultipleCommentsView> getComments(final String slug, final Optional<User> user) {
        return this.userArticleService.getComments(slug, user);
    }

    public Mono<ArticleView> favoriteArticle(final String slug, final User actionUser) {
        return updateArticleOnAction(slug, actionUser, ArticleAction.FAVORITE);
    }

    public Mono<ArticleView> unfavoriteArticle(final String slug, final User actionUser) {
        return updateArticleOnAction(slug, actionUser, ArticleAction.UNFAVORITE);
    }

    private Mono<ArticleView> updateArticleOnAction(final String slug, final User actionUser, final ArticleAction action) {
        return this.articleRepository.findBySlug(slug)
                .flatMap(article -> {
                    if (action.act(article, actionUser)) {
                        return this.articleRepository.save(article);
                    }
                    return Mono.just(article);
                })
                .flatMap(r -> this.userArticleService.mapToArticleView(r, actionUser));
    }


    enum ArticleAction {
        FAVORITE,
        UNFAVORITE;

        public boolean act(final Article article, final User actionUser) {
            switch(this) {
                case FAVORITE: return article.favoriteByUser(actionUser);
                case UNFAVORITE: return article.unfavoriteByUser(actionUser);
                default: return false;
            }
        }
    }

}
