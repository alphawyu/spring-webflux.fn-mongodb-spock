package com.realworld.webfluxfn.service.article

import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.dto.request.CreateCommentRequest
import com.realworld.webfluxfn.dto.view.ArticleView
import com.realworld.webfluxfn.dto.view.CommentView
import com.realworld.webfluxfn.dto.view.MultipleArticlesView
import com.realworld.webfluxfn.dto.view.MultipleCommentsView
import com.realworld.webfluxfn.dto.view.ProfileView
import com.realworld.webfluxfn.exception.InvalidRequestException
import com.realworld.webfluxfn.persistence.entity.Article
import com.realworld.webfluxfn.persistence.entity.Comment
import com.realworld.webfluxfn.persistence.entity.User
import com.realworld.webfluxfn.persistence.repository.ArticleRepository
import com.realworld.webfluxfn.persistence.repository.UserRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import static junit.framework.TestCase.assertTrue
import static junit.framework.TestCase.assertEquals

class UserArticleServiceTest extends Specification {
    private final ArticleRepository articleRepository = Mock()
    private final UserRepository userRepository = Mock()

    private final UserArticleService userArticleService = new UserArticleService(articleRepository, userRepository)

    void setup() {
    }

    void cleanup() {
    }

    def "test FindArticles success"() {
        given: "an an existing article's tag, author name, favored by user name, offset, limit, and an optional current user"
        ArticleView article_1 =  MockTestData.ARTICLE_VIEW_1
        ProfileView expectedAuthorProfile = MockTestData.makeUserProfile(MockTestData.AUTHOR_USER)
        expectedAuthorProfile.setFollowing(false)
        int offset = 0
        int limit = 10
        String tag = uHasTag ? article_1.getTags().get(0) : null
        User favoritingUser = uHasFavoritingUserName? MockTestData.CURRENT_USER : null
        String favoritingUserName = uHasFavoritingUserName ? favoritingUser.getUsername() : null
        User authorUser = uHasAuthorName ? MockTestData.AUTHOR_USER : null
        String authorUserId = uHasAuthorName ? authorUser.getId() : null
        String authorName = uHasAuthorName? authorUser.getUsername() : null
        and:
        userRepository.findByUsername(favoritingUserName) >> (uHasFavoritingUserName? Mono.just(favoritingUser) : Mono.empty())
        userRepository.findByUsername(authorName) >> (uHasAuthorName ? Mono.just(authorUser) : Mono.empty())
        articleRepository.findNewestArticlesFilteredBy(tag, authorUserId, favoritingUser, limit, offset)
                >> Flux.just(MockTestData.ARTICLE_1, MockTestData.ARTICLE_2)
        userRepository.findAuthorByArticle(MockTestData.ARTICLE_1) >> Mono.just(MockTestData.AUTHOR_USER)
        userRepository.findAuthorByArticle(MockTestData.ARTICLE_2) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service call the findArticles()"
        Mono<MultipleArticlesView> rtn = userArticleService.findArticles(tag, (String) authorName, (String) favoritingUserName, offset, limit, Optional.of(MockTestData.CURRENT_USER))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(2, r.getArticlesCount())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTitle(), r.getArticles().get(0).getTitle())
                    assertEquals(expectedAuthorProfile, r.getArticles().get(0).getAuthor())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getBody(), r.getArticles().get(0).getBody())
                    assertEquals(MockTestData.ARTICLE_VIEW_1.getTags(), r.getArticles().get(0).getTags())
                    assertEquals(MockTestData.ARTICLE_VIEW_2.getTitle(), r.getArticles().get(1).getTitle())
                    assertEquals(MockTestData.ARTICLE_VIEW_2.getTags(), r.getArticles().get(1).getTags())
                    assertEquals(expectedAuthorProfile, r.getArticles().get(1).getAuthor())
                    assertEquals(MockTestData.ARTICLE_VIEW_2.getBody(), r.getArticles().get(1).getBody())
                })
                .expectComplete().verify()

        where:
        uHasTag  | uHasAuthorName | uHasFavoritingUserName
        true     | true           | true
        false    | true           | true
        true     | false          | true
        true     | true           | false
    }

    def "test AddComment success"() {
        given: "an existing article's slug, a CreateCommentRequest, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        CommentView expectedCommentView = MockTestData.COMMENT_VIEW_11
        CreateCommentRequest createCommentRequest = new CreateCommentRequest(expectedCommentView.getBody())
        and:
        articleRepository.findBySlugOrFail(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.save(_) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the addComment()"
        Mono<CommentView> rtn = userArticleService.addComment(slug, Mono.just(createCommentRequest), currUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(r.getBody(), expectedCommentView.getBody())
                    assertEquals(r.getAuthor().getUsername(), expectedCommentView.getAuthor().getUsername())
                    assertEquals(r.getAuthor().getBio(), expectedCommentView.getAuthor().getBio())
                    assertEquals(r.getAuthor().getImage(), expectedCommentView.getAuthor().getImage())
                })
                .expectComplete().verify()
    }

    def "test DeleteComment success"() {
        given: "an existing article's slug, a comment id, a current user"
        User authorUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        Comment expectedComment = MockTestData.COMMENT_11
        String commentId = expectedComment.getId()
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_11)
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_12)
        and:
        articleRepository.findBySlugOrFail(slug) >> Mono.just(MockTestData.ARTICLE_1)
        articleRepository.save(_) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the deleteComment()"
        Mono<Void> rtn = userArticleService.deleteComment(commentId, slug, authorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectComplete().verify()
    }

    def "test DeleteComment failed because not requested by author"() {
        given: "an existing article's slug, a comment id, a current user"
        User nonCommentAuthorUser = MockTestData.makeUser("non-comment-author")
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        Comment expectedComment = MockTestData.COMMENT_11
        String commentId = expectedComment.getId()
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_11)
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_12)
        and:
        articleRepository.findBySlugOrFail(slug) >> Mono.just(MockTestData.ARTICLE_1)

        when: "the service call the deleteComment()"
        Mono<Void> rtn = userArticleService.deleteComment(commentId, slug, nonCommentAuthorUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof InvalidRequestException)
                }).verify()
    }

    def "test GetComments success"() {
        given: "an existing article's slug, a current user"
        User currUser = MockTestData.CURRENT_USER
        ArticleView articleView1 = MockTestData.ARTICLE_VIEW_1
        String slug = articleView1.getSlug()
        MockTestData.ARTICLE_1.getComments().clear()
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_11)
        MockTestData.ARTICLE_1.getComments().add(MockTestData.COMMENT_12)
        and:
        articleRepository.findBySlug(slug) >> Mono.just(MockTestData.ARTICLE_1)
        userRepository.findById(MockTestData.ARTICLE_1.getAuthorId()) >> Mono.just(MockTestData.CURRENT_USER)

        when: "the service call the getComments()"
        Mono<MultipleCommentsView> rtn = userArticleService.getComments(slug, Optional.of(currUser))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(2, r.getComments().size())
                    assertEquals(MockTestData.COMMENT_VIEW_11.getId(), r.getComments().get(0).getId())
                    assertEquals(MockTestData.COMMENT_VIEW_11.getBody(), r.getComments().get(0).getBody())
                    assertEquals(MockTestData.COMMENT_VIEW_11.getAuthor().getUsername(), r.getComments().get(0).getAuthor().getUsername())
                    assertEquals(MockTestData.COMMENT_VIEW_11.getAuthor().getBio(), r.getComments().get(0).getAuthor().getBio())
                    assertEquals(MockTestData.COMMENT_VIEW_11.getAuthor().getImage(), r.getComments().get(0).getAuthor().getImage())
                    assertEquals(MockTestData.COMMENT_VIEW_12.getId(), r.getComments().get(1).getId())
                    assertEquals(MockTestData.COMMENT_VIEW_12.getBody(), r.getComments().get(1).getBody())
                    assertEquals(MockTestData.COMMENT_VIEW_12.getAuthor().getUsername(), r.getComments().get(1).getAuthor().getUsername())
                    assertEquals(MockTestData.COMMENT_VIEW_12.getAuthor().getBio(), r.getComments().get(1).getAuthor().getBio())
                    assertEquals(MockTestData.COMMENT_VIEW_12.getAuthor().getImage(), r.getComments().get(1).getAuthor().getImage())
                })
                .expectComplete().verify()
    }

    def "test MapToArticleView with optional viewer user"() {
        given: "an article, a viewer user"
        User viewerUser = MockTestData.CURRENT_USER
        Article article = MockTestData.ARTICLE_1
        String slug = article.getSlug()
        ArticleView expectedArticleView = MockTestData.ARTICLE_VIEW_1
        ProfileView expectedAuthorProfile = MockTestData.makeUserProfile(MockTestData.AUTHOR_USER)
        expectedAuthorProfile.setFollowing(false)
        and:
        userRepository.findAuthorByArticle(article) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service call the mapToArticleView()"
        Mono<ArticleView> rtn = userArticleService.mapToArticleView(article, Optional.of(viewerUser))

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(expectedArticleView.getSlug(), r.getSlug())
                    assertEquals(expectedArticleView.getTitle(), r.getTitle())
                    assertEquals(expectedArticleView.getDescription(), r.getDescription())
                    assertEquals(expectedArticleView.getBody(), r.getBody())
                    assertEquals(expectedArticleView.getTags(), r.getTags())
                    assertEquals(expectedAuthorProfile, r.getAuthor())
                })
                .expectComplete().verify()
    }

    def "test MapToArticleView with view user"() {
        given: "an article, a viewer user"
        User viewerUser = MockTestData.CURRENT_USER
        Article article = MockTestData.ARTICLE_1
        String slug = article.getSlug()
        ArticleView expectedArticleView = MockTestData.ARTICLE_VIEW_1
        ProfileView expectedAuthorProfile = MockTestData.makeUserProfile(MockTestData.AUTHOR_USER)
        expectedAuthorProfile.setFollowing(false)
        and:
        userRepository.findAuthorByArticle(article) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service call the mapToArticleView()"
        Mono<ArticleView> rtn = userArticleService.mapToArticleView(article, viewerUser)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(expectedArticleView.getSlug(), r.getSlug())
                    assertEquals(expectedArticleView.getTitle(), r.getTitle())
                    assertEquals(expectedArticleView.getDescription(), r.getDescription())
                    assertEquals(expectedArticleView.getBody(), r.getBody())
                    assertEquals(expectedArticleView.getTags(), r.getTags())
                    assertEquals(expectedAuthorProfile, r.getAuthor())
                })
                .expectComplete().verify()
    }

    def "test MapToArticleView without user"() {
        given: "an article, a viewer user"
        User viewerUser = MockTestData.CURRENT_USER
        Article article = MockTestData.ARTICLE_1
        String slug = article.getSlug()
        ArticleView expectedArticleView = MockTestData.ARTICLE_VIEW_1
        and:
        userRepository.findAuthorByArticle(article) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service call the mapToArticleView()"
        Mono<ArticleView> rtn = userArticleService.mapToArticleView(article)

        then: "return contains following"
        StepVerifier.create(rtn)
                .assertNext(r -> {
                    assertEquals(expectedArticleView.getSlug(), r.getSlug())
                    assertEquals(expectedArticleView.getTitle(), r.getTitle())
                    assertEquals(expectedArticleView.getDescription(), r.getDescription())
                    assertEquals(expectedArticleView.getBody(), r.getBody())
                    assertEquals(expectedArticleView.getTags(), r.getTags())
                    assertEquals(expectedArticleView.getAuthor().getUsername(), r.getAuthor().getUsername())
                    assertEquals(false, r.getAuthor().isFollowing())
                })
                .expectComplete().verify()
    }
}
