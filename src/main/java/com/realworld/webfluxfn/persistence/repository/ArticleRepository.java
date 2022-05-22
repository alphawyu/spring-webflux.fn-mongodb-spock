package com.realworld.webfluxfn.persistence.repository;

import com.realworld.webfluxfn.persistence.OffsetBasedPageable;
import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.exception.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ArticleRepository extends ReactiveMongoRepository<Article, String>, ArticleManualRepository {
    Sort NEWEST_ARTICLE_SORT = Sort.by(Article.CREATED_AT_FIELD_NAME).descending();

    Flux<Article> findMostRecentByAuthorIdIn(Collection<String> authorId, Pageable pageable);

    Mono<Article> findBySlug(String slug);

    Mono<Article> deleteArticleBySlug(String slug);

    default Flux<Article> findNewestArticlesByAuthorIds(final Collection<String> authorId, final int offset, final int limit) {
        return findMostRecentByAuthorIdIn(authorId, OffsetBasedPageable.makeInstance(limit, offset, NEWEST_ARTICLE_SORT));
    }

    default Mono<Article> findBySlugOrFail(final String slug) {
        return findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")));
    }
}
