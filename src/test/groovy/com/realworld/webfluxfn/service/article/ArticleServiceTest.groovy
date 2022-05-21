package com.realworld.webfluxfn.service.article

import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.dto.request.CreateArticleRequest
import com.realworld.webfluxfn.dto.request.CreateCommentRequest
import com.realworld.webfluxfn.dto.request.UpdateArticleRequest
import com.realworld.webfluxfn.dto.view.ArticleView
import com.realworld.webfluxfn.dto.view.CommentView
import com.realworld.webfluxfn.dto.view.MultipleArticlesView
import com.realworld.webfluxfn.dto.view.MultipleCommentsView
import com.realworld.webfluxfn.dto.view.TagListView
import com.realworld.webfluxfn.exception.InvalidRequestException
import com.realworld.webfluxfn.persistence.entity.Article
import com.realworld.webfluxfn.persistence.entity.Tag
import com.realworld.webfluxfn.persistence.entity.User
import com.realworld.webfluxfn.persistence.repository.ArticleRepository
import com.realworld.webfluxfn.persistence.repository.TagRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.assertFalse
import static junit.framework.TestCase.assertTrue

class ArticleServiceTest extends Specification {
    private final ArticleRepository articleRepository = Mock()
    private final TagRepository tagRepository = Mock()
    private final UserArticleService userArticleService = Mock()

    ArticleService articleService = new ArticleService(
            articleRepository, tagRepository, userArticleService)

    void setup() {
        Hooks.onOperatorDebug()
    }

    void cleanup() {
    }

    def "test GetTags success"() {
        given: "application context has following tags"
        Tag tag1 = MockTestData.TAG_1
        Tag tag2 = MockTestData.TAG_2
        Tag tag3 = MockTestData.TAG_3
        and: "tag repository returns value on findAll call"
        tagRepository.findAll () >> Flux.just(tag1, tag2, tag3)

        when: "retrieve all the tags"
        Mono<TagListView> rtn = articleService.getTags()

        then: "the service calls the tag repository for data"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(r.getTags().size(), 3)
                    assertEquals(r.getTags().get(0), MockTestData.TAG_1.getTagName())
                    assertEquals(r.getTags().get(1), MockTestData.TAG_2.getTagName())
                    assertEquals(r.getTags().get(2), MockTestData.TAG_3.getTagName())
                })
                .expectComplete().verify()
    }

    def "test CreateArticle success"() {
        given: "a create article request, and author"
        CreateArticleRequest request = new CreateArticleRequest()
                .setTitle(MockTestData.ARTICLE_VIEW_1.getTitle())
                .setDescription(MockTestData.ARTICLE_VIEW_1.getDescription())
                .setBody(MockTestData.ARTICLE_VIEW_1.getBody())
                .setTagList(MockTestData.ARTICLE_VIEW_1.getTags())
        User author = MockTestData.AUTHOR_USER
        and:
        articleRepository.saveAll(_) >> Flux.just(MockTestData.ARTICLE_1)
        tagRepository.saveAllTags(MockTestData.ARTICLE_1.getTags()) >> Flux.just(MockTestData.TAG_1, MockTestData.TAG_2)

        when: "the service calls to create article"
        println(MockTestData.ARTICLE_1.getSlug())
        Mono<ArticleView> rtn = articleService.createArticle(Mono.just(request), author)

        then: "the service calls article repository saveAll"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getSlug(), r.getSlug())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTitle(), r.getTitle())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getDescription(), r.getDescription())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getBody(), r.getBody())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTags(), r.getTags())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getAuthor().getUsername(), r.getAuthor().getUsername())
                })
                .expectComplete().verify()
    }

    def "test Feed success"() {
        given: "an author, and the feed limit, offset"
        User currUser = MockTestData.CURRENT_USER
        int offset = 0
        int limit = 10
        and:
        articleRepository.findNewestArticlesByAuthorIds(MockTestData.CURRENT_USER.getFollowingIds(), offset, limit)
                >> Flux.just(MockTestData.ARTICLE_1, MockTestData.ARTICLE_2)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_1, currUser) >> Mono.just(MockTestData.ARTICLE_VIEW_1)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_2, currUser) >> Mono.just(MockTestData.ARTICLE_VIEW_2)

        when: "the service call feed of the author user"
        Mono<MultipleArticlesView> rtn = articleService.feed(offset, limit, currUser)

        then: "the service calls"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(r.getArticlesCount(), 2)
                    assertEquals(r.getArticles().get(0), MockTestData.ARTICLE_VIEW_1)
                    assertEquals(r.getArticles().get(1), MockTestData.ARTICLE_VIEW_2)
                })
                .expectComplete().verify()
    }

    def "test FindArticles success"() {
        given: "an an existing article's tag, author name, favorited by user name, offset, limit, and an optional current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView article_1 = MockTestData.ARTICLE_VIEW_1
        String tag = article_1.getTags().get(0)
        String authorName = article_1.getAuthor().getUsername()
        String usernName = currUser.getUsername()
        int offset = 0
        int limit = 10
        and:
        userArticleService.findArticles(tag, authorName, usernName, offset, limit, Optional.of(currUser))
                >> Mono.just(MultipleArticlesView.makeInstance([MockTestData.ARTICLE_1, MockTestData.ARTICLE_2]))

        when: "the service call the findArticles()"
//        Mono<MultipleArticlesView> rtn = articleService.findArticles(null, null, null, offset, limit, Optional.of(currUser))
        Mono<MultipleArticlesView> rtn = articleService.findArticles(tag, authorName, usernName, offset, limit, Optional.of(currUser))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(r.getArticlesCount(), 2)
                    assertEquals(r.getArticles().get(0), MockTestData.ARTICLE_1)
                    assertEquals(r.getArticles().get(1), MockTestData.ARTICLE_2)
                })
                .expectComplete().verify()

    }

    def "test GetArticle success"() {
        given: "an an existing article's slug, and an optional current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_1, Optional.of(currUser)) >> Mono.just(articleView1)

        when: "the service call the getArticle()"
        Mono<ArticleView> rtn = articleService.getArticle(slug, Optional.of(currUser))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(articleView1, r)
                })
                .expectComplete().verify()
    }

    def "test UpdateArticle success"() {
        given: "an existing article's slug, updateArticleRequest and an author user"
        User authorUser = MockTestData.AUTHOR_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        UpdateArticleRequest updateArticleRequest = UpdateArticleRequest.builder()
                .title(articleView1.getTitle()).description(articleView1.getDescription()).body(articleView1.getBody()).build()
        and:
        articleRepository.findBySlugOrFail(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.save(_) >> Mono.just(MockTestData.ARTICLE_1)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_1, authorUser) >> Mono.just(articleView1)

        when: "the service call the updateArticle()"
        Mono<ArticleView> rtn = articleService.updateArticle(slug, Mono.just(updateArticleRequest), authorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(MockTestData.ARTICLE_VIEW_1, r)
                })
                .expectComplete().verify()
    }

    def "test UpdateArticle fail because not not requested by the author"() {
        given: "an existing article's slug, updateArticleRequest and an author user"
        User nonAuthorUser = MockTestData.CURRENT_USER
        String slug = MockTestData.ARTICLE_1.getSlug()
        UpdateArticleRequest updateArticleRequest = UpdateArticleRequest.builder()
                .title(MockTestData.ARTICLE_1.getTitle())
                .description(MockTestData.ARTICLE_1.getDescription())
                .body(MockTestData.ARTICLE_1.getBody()).build()
        and:
        articleRepository.findBySlugOrFail(slug) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the updateArticle()"
        Mono<ArticleView> rtn = articleService.updateArticle(slug, Mono.just(updateArticleRequest), nonAuthorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectErrorSatisfies(ex-> {
                    assertTrue(ex instanceof InvalidRequestException)
                }).verify()
    }

    def "test DeleteArticle success"() {
        given: "an existing article's slug, an author user"
        User authorUser = MockTestData.AUTHOR_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.deleteArticleBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the deleteArticle()"
        Mono<Void> rtn = articleService.deleteArticle(slug, authorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectComplete().verify()
    }

    def "test DeleteArticle fail because not not requested by the author"() {
        given: "an existing article's slug, an author user"
        User nonAuthorUser = MockTestData.CURRENT_USER
        String slug = MockTestData.ARTICLE_1.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the deleteArticle()"
        Mono<Void> rtn = articleService.deleteArticle(slug, nonAuthorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectErrorSatisfies(ex-> {
                    assertTrue(ex instanceof InvalidRequestException)
                }).verify()
    }

    def "test AddComment success"() {
        given: "an existing article's slug, a CreateCommentRequest, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        CommentView expectedCommentView = MockTestData.COMMENT_VIEW_11
        CreateCommentRequest createCommentRequest = new CreateCommentRequest(expectedCommentView.getBody())
        and:
        userArticleService.addComment(slug, _, currUser) >> Mono.just(expectedCommentView)

        when: "the service call the addComment()"
        Mono<CommentView> rtn = articleService.addComment(slug, Mono.just(createCommentRequest), currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(expectedCommentView, r)
                })
                .expectComplete().verify()
    }

    def "test DeleteComment success"() {
        given: "an existing article's slug, a comment id, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        CommentView expectedCommentView = MockTestData.COMMENT_VIEW_11
        String commentId = expectedCommentView.getId()
        and:
        userArticleService.deleteComment(commentId, slug, currUser) >> Mono.empty()

        when: "the service call the deleteComment()"
        Mono<Void> rtn = articleService.deleteComment(commentId, slug, currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectComplete().verify()
    }

    def "test GetComments success"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        and:
        userArticleService.getComments(slug, Optional.of(currUser))
                >> Mono.just(MultipleCommentsView.makeInstance([MockTestData.COMMENT_11, MockTestData.COMMENT_12]))

        when: "the service call the GetComments()"
        Mono<MultipleCommentsView> rtn = articleService.getComments(slug, Optional.of(currUser))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(r.getComments().size(), 2)
                    assertEquals(r.getComments().get(0), MockTestData.COMMENT_11)
                    assertEquals(r.getComments().get(1), MockTestData.COMMENT_12)
                })
                .expectComplete().verify()
    }

    def "test FavoriteArticle success"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 =MockTestData.makeArticleView(MockTestData.ARTICLE_1, MockTestData.AUTHOR_USER_PROFILE)
        articleView1.setFavorited(true)
        String slug = articleView1.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.save(_) >> Mono.just(MockTestData.ARTICLE_1)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_1, currUser) >> Mono.just(articleView1)

        when: "the service call the findArticles()"
        Mono<ArticleView> rtn = articleService.favoriteArticle(slug, currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getSlug(), r.getSlug())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTitle(), r.getTitle())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getDescription(), r.getDescription())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getBody(), r.getBody())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTags(), r.getTags())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getAuthor().getUsername(), r.getAuthor().getUsername())
                    assertTrue(r.getFavorited())
                })
                .expectComplete().verify()
    }

    def "test FavoriteArticle already favorited"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        Article testArticle = MockTestData.makeArticle("to-favorite", MockTestData.AUTHOR_USER,
                null, null, [currUser.getId()])
        ArticleView testArticleView =MockTestData.makeArticleView(testArticle, MockTestData.AUTHOR_USER_PROFILE)
        testArticleView.setFavorited(true)
        String slug = testArticle.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(testArticle)
        userArticleService.mapToArticleView(testArticle, currUser) >> Mono.just(testArticleView)

        when: "the service call the findArticles()"
        Mono<ArticleView> rtn = articleService.favoriteArticle(slug, currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(testArticleView.getSlug(), r.getSlug())
                    assertEquals(testArticleView.getTitle(), r.getTitle())
                    assertEquals(testArticleView.getDescription(), r.getDescription())
                    assertEquals(testArticleView.getBody(), r.getBody())
                    assertEquals(testArticleView.getTags(), r.getTags())
                    assertEquals(testArticleView.getAuthor().getUsername(), r.getAuthor().getUsername())
                    assertTrue(r.getFavorited())
                })
                .expectComplete().verify()
    }

    def "test UnfavoriteArticle success"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 =MockTestData.makeArticleView(MockTestData.ARTICLE_1, MockTestData.AUTHOR_USER_PROFILE)
        articleView1.setFavorited(false)
        String slug = articleView1.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.save(_) >> Mono.just(MockTestData.ARTICLE_1)
        userArticleService.mapToArticleView(MockTestData.ARTICLE_1, currUser) >> Mono.just(articleView1)

        when: "the service call the findArticles()"
        Mono<ArticleView> rtn = articleService.unfavoriteArticle(slug, currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getSlug(), r.getSlug())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTitle(), r.getTitle())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getDescription(), r.getDescription())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getBody(), r.getBody())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTags(), r.getTags())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getAuthor().getUsername(), r.getAuthor().getUsername())
                    assertFalse(r.getFavorited())
                })
                .expectComplete().verify()
    }

    def "test UnfavoriteArticle not favorited"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        Article testArticle = MockTestData.makeArticle("to-unfavorite", MockTestData.AUTHOR_USER,
                null, null, null)
        ArticleView testArticleView =MockTestData.makeArticleView(testArticle, MockTestData.AUTHOR_USER_PROFILE)
        testArticleView.setFavorited(false)
        String slug = testArticle.getSlug()
        and:
        articleRepository.findBySlug(slug) >> Mono.just(testArticle)
        userArticleService.mapToArticleView(testArticle, currUser) >> Mono.just(testArticleView)

        when: "the service call the findArticles()"
        Mono<ArticleView> rtn = articleService.unfavoriteArticle(slug, currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(testArticleView.getSlug(), r.getSlug())
                    assertEquals(testArticleView.getTitle(), r.getTitle())
                    assertEquals(testArticleView.getDescription(), r.getDescription())
                    assertEquals(testArticleView.getBody(), r.getBody())
                    assertEquals(testArticleView.getTags(), r.getTags())
                    assertEquals(testArticleView.getAuthor().getUsername(), r.getAuthor().getUsername())
                    assertFalse(r.getFavorited())
                })
                .expectComplete().verify()
    }
}
