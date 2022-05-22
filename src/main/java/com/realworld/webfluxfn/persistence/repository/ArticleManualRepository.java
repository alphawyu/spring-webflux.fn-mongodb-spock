package com.realworld.webfluxfn.persistence.repository;

import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.dto.request.FindArticlesRequest;
import com.realworld.webfluxfn.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public interface ArticleManualRepository {
    Flux<Article> findNewestArticlesFilteredBy(@Nullable String tag,
                                               @Nullable String authorId,
                                               @Nullable User favoritingUser,
                                               int limit,
                                               int offset);

    default Flux<Article> findNewestArticlesFilteredBy(final FindArticlesRequest request) {
        return findNewestArticlesFilteredBy(request.getTag(),
                request.getAuthorId(),
                request.getFavoritedBy(),
                request.getLimit(),
                request.getOffset());
    }
}

@RequiredArgsConstructor
@Slf4j
class ArticleManualRepositoryImpl implements ArticleManualRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<Article> findNewestArticlesFilteredBy(@Nullable final String tag,
                                                      @Nullable final String authorId,
                                                      @Nullable final User favoritingUser,
                                                      final int limit,
                                                      final int offset) {
        final Query query = new Query()
                .skip(offset)
                .limit(limit)
                .with(ArticleRepository.NEWEST_ARTICLE_SORT);
        ofNullable(favoritingUser)
                .ifPresent(user -> query.addCriteria(favoritedByUser(user)));
        ofNullable(tag)
                .ifPresent(it -> query.addCriteria(tagsContains(it)));
        ofNullable(authorId)
                .ifPresent(it -> query.addCriteria(authorIdEquals(it)));
        return mongoTemplate.find(query, Article.class);
    }

    private static Criteria authorIdEquals(final String it) {
        return where(Article.AUTHOR_ID_FIELD_NAME).is(it);
    }

    private static Criteria tagsContains(final String it) {
        return where(Article.TAGS_FIELD_NAME).all(it);
    }

    private static Criteria favoritedByUser(final User it) {
        return where(Article.FAVORITING_USER_IDS).all(it.getId());
    }
}
