package com.realworld.testharness.article;

import com.realworld.testharness.user.UserSamples;
import com.realworld.webfluxfn.dto.request.CreateArticleRequest;
import com.realworld.webfluxfn.persistence.entity.Article;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ArticleSamples {

    public static final String DEFAULT_ARTICLE_BODY = "test article body";
    public static final String DEFAULT_ARTICLE_DESCRIPTION = "test article description";
    public static final String DEFAULT_ARTICLE_TITLE = "test article title";
    public static final List<String> DEFAULT_TAG_LIST = List.of("test_tag_1", "test_tag_2", "test_tag_3");

    public static CreateArticleRequest sampleCreateArticleRequest() {
        return new CreateArticleRequest()
                .setBody(DEFAULT_ARTICLE_BODY)
                .setDescription(DEFAULT_ARTICLE_DESCRIPTION)
                .setTitle(DEFAULT_ARTICLE_TITLE)
                .setTagList(DEFAULT_TAG_LIST);
    }

    public static Article.ArticleBuilder sampleArticle() {
        return Article.builder()
                .id(UUID.randomUUID().toString())
                .body(DEFAULT_ARTICLE_BODY)
                .description(DEFAULT_ARTICLE_DESCRIPTION)
                .title(DEFAULT_ARTICLE_TITLE)
                .tags(DEFAULT_TAG_LIST)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .authorId(UserSamples.SAMPLE_USER_ID)
                .favoritingUserIds(null);
    }
}
