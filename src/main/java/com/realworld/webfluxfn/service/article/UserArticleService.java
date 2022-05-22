package com.realworld.webfluxfn.service.article;

import com.realworld.webfluxfn.dto.request.CreateCommentRequest;
import com.realworld.webfluxfn.dto.view.*;
import com.realworld.webfluxfn.exception.InvalidRequestException;
import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.persistence.repository.ArticleRepository;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.realworld.webfluxfn.dto.view.ArticleView.toArticleViewForViewer;
import static com.realworld.webfluxfn.dto.view.ProfileView.convertToProfileViewByViewerUser;
import static com.realworld.webfluxfn.dto.view.ProfileView.toUnfollowedProfileView;

@Component
@RequiredArgsConstructor
@Slf4j
class UserArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Mono<MultipleArticlesView> findArticles(final String tag, final String authorName, final String favoritingUserName,
                                                   final int offset, final int limit, final Optional<User> currentUser) {
        if (StringUtils.isEmpty(favoritingUserName)) {
            return findArticles(tag, authorName, (User)null, offset, limit, currentUser);
        }
        return userRepository.findByUsername(favoritingUserName)
                .flatMap(fu -> this.findArticles(tag, authorName, fu, offset, limit, currentUser));
    }

    private Mono<MultipleArticlesView> findArticles(final String tag, final String authorName, final User favoritingUser,
                                                    final int offset, final int limit, final Optional<User> currentUser) {
        if (StringUtils.isEmpty(authorName)) {
            return this.findArticles(tag, (User) null, favoritingUser, offset, limit, currentUser);
        }
        return userRepository.findByUsername(authorName).
                flatMap(au -> this.findArticles(tag, au, favoritingUser, offset, limit, currentUser));
    }

    private Mono<MultipleArticlesView> findArticles(final String tag, final User authorUser, final User favoritingUser,
                                                    final int offset, final int limit, final Optional<User> currentUser) {
        final String authorUserId = (authorUser == null) ? null : authorUser.getId();
        return articleRepository.findNewestArticlesFilteredBy(tag, authorUserId, favoritingUser, limit, offset)
                    .flatMap(article -> this.mapToArticleView(article, currentUser))
                    .collectList()
                    .map(MultipleArticlesView::makeInstance);
    }

    public Mono<CommentView> addComment(final String slug, final Mono<CreateCommentRequest> request, final User currentUser) {
        return request.map(req -> req.toComment(UUID.randomUUID().toString(), currentUser.getId()))
                        .flatMap(comment -> articleRepository
                                .findBySlugOrFail(slug)
                                .flatMap(article -> {
                                    article.addComment(comment);
                                    return articleRepository.save(article);
                                }).thenReturn(comment))
                .map(c -> CommentView.toCommentView(c, ProfileView.toOwnProfile(currentUser)));
    }

    public Mono<Void> deleteComment(final String commentId, final String slug, final User user) {
        return this.articleRepository.findBySlugOrFail(slug)
                .flatMap(article -> Mono.justOrEmpty(article.getCommentById(commentId))
                        .flatMap(comment -> comment.isAuthor(user)
                                ? Mono.just(comment)
                                : Mono.error(new InvalidRequestException("Comment", "only author can delete comment")))
                        .flatMap(comment -> {
                            article.deleteComment(comment);
                            return this.articleRepository.save(article).then();
                        })
                );
    }

    public Mono<MultipleCommentsView> getComments(final String slug, final Optional<User> viewerUser) {
        return this.articleRepository.findBySlug(slug)
                .zipWhen(article -> userRepository.findById(article.getAuthorId()))
                .map(tuple -> {
                    final Article article = tuple.getT1();
                    final User authorUser = tuple.getT2();
                    final ProfileView authorProfile = viewerUser
                            .map(vu -> convertToProfileViewByViewerUser(authorUser, vu))
                            .orElse(toUnfollowedProfileView(authorUser));
                    final List<CommentView> commentViews = article.getComments().stream()
                            .map(comment -> CommentView.toCommentView(comment, authorProfile))
                            .toList();
                    return MultipleCommentsView.makeInstance(commentViews);
                });
    }

    public Mono<ArticleView> mapToArticleView(final Article article, final Optional<User> viewer) {
        return viewer.map(user -> mapToArticleView(article, user))
                .orElse(Mono.defer(() -> mapToArticleView(article)));
    }

    public Mono<ArticleView> mapToArticleView(final Article article, final User user) {
        return this.userRepository.findAuthorByArticle(article)
                .doOnNext(au -> log.info("article author is found as " + au))
                .map(au -> toArticleViewForViewer(article, convertToProfileViewByViewerUser(au, user), user));
    }

    public Mono<ArticleView> mapToArticleView(final Article article) {
        return this.userRepository.findAuthorByArticle(article)
                .map(author -> ArticleView.toUnfavoredArticleView(article, toUnfollowedProfileView(author)));
    }
}
