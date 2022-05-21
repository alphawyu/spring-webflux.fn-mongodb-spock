package com.realworld.webfluxfn.api


import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.dto.request.CreateArticleRequest
import com.realworld.webfluxfn.dto.view.MultipleArticlesView
import com.realworld.webfluxfn.dto.view.MultipleCommentsView
import com.realworld.webfluxfn.dto.view.TagListView
import com.realworld.webfluxfn.persistence.entity.Tag
import com.realworld.webfluxfn.service.user.UserSessionProvider
import com.realworld.webfluxfn.service.article.ArticleService
import org.spockframework.spring.SpringBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import static org.springframework.web.reactive.function.server.RequestPredicates.GET
import static org.springframework.web.reactive.function.server.RequestPredicates.POST
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT

@Title("test Article Handler api")
class ArticleHandlerTest extends Specification {
    final static String PATH_PREFIX = '/api2'
    @SpringBean
    private final ArticleService articleService = Mock()
    @SpringBean
    private final UserSessionProvider userSessionProvider = Mock()

    @SpringBean
    private final ArticleHandler articleHandler = new ArticleHandler(articleService, userSessionProvider)

    void setup() {
    }

    void cleanup() {
    }

    def "test CreateArticle success"() {
        given: 'an article'
        String jsonStr = """{ 
            "article": {
               "title": "title1",
                "description": "description1",
                "body": "body1",
                "tagList": ["tag11", "tag12", "tag13"]
            }
        }"""

        when: 'api alls articale service to save the article'
        String testingPath = PATH_PREFIX + "/articles"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath), articleHandler::createArticle))
                .configureClient().build()
        var result = client.post().uri(testingPath).bodyValue(jsonStr).exchange()

        then: 'invokes create article and return article view '
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.createArticle(_  as Mono<CreateArticleRequest>, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.ARTICLE_VIEW_1)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.article.title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.article.description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.article.body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.article.tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
                .jsonPath('$.article.favorited').isEqualTo(MockTestData.ARTICLE_VIEW_1.getFavorited())
    }

    @Unroll
    def "test GetArticles success"() {
        given: 'user login successfully'
        String qstr = uQStr
        int offset = uOffset
        int limit = uLimit
        String tag = uTag
        String username = uUserName
        String authorName = uAuthor

        when: 'call api to retrieve all the articles'
        String testingPath = PATH_PREFIX + "/articles"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath), articleHandler::getArticles))
                .configureClient().build()
        var result = client.get().uri(testingPath + qstr).exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.findArticles(tag, authorName, username, offset, limit, Optional.of(MockTestData.CURRENT_USER))
                >> Mono.just(MultipleArticlesView.makeInstance([MockTestData.ARTICLE_VIEW_1, MockTestData.ARTICLE_VIEW_2]))
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.articles[0].slug').isEqualTo(MockTestData.ARTICLE_VIEW_1.getSlug())
                .jsonPath('$.articles[0].title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.articles[0].description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.articles[0].body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.articles[0].tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
                .jsonPath('$.articles[0].favorited').isEqualTo(MockTestData.ARTICLE_VIEW_1.getFavorited())
                .jsonPath('$.articles[0].favoritesCount').isEqualTo(MockTestData.ARTICLE_VIEW_1.getFavoritesCount())
                .jsonPath('$.articles[0].author.username').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getUsername())
                .jsonPath('$.articles[0].author.bio').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getBio())
                .jsonPath('$.articles[0].author.image').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getImage())
                .jsonPath('$.articles[0].author.following').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().isFollowing())
                .jsonPath('$.articles[1].author.username').isEqualTo(MockTestData.ARTICLE_VIEW_2.getAuthor().getUsername())

        where:
        uQStr                                                        || uOffset | uLimit | uTag    | uUserName | uAuthor
        '''?offset=0&limit=10&tag=tag1&favorited=me&author=author''' || 0       | 10     | "tag1"  | "me"      | "author"
        '''?offset=5'''                                              || 5       | 20     | null    | null      | null
    }

    @Unroll
    def "test Feed success"() {
        given: 'paging  parameter '
        String qstr = uQStr
        int offset = uOffset
        int limit = uLimit

        when: 'calls api for the feed of current user'
        String testingPath = PATH_PREFIX + "/articles/feed"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath), articleHandler::feed))
                .configureClient().build()
        var result = client.get().uri(testingPath + qstr).exchange()

        then: 'the article service looks up the feed for the current user'
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.feed(offset, limit, MockTestData.CURRENT_USER)
                >> Mono.just(MultipleArticlesView.makeInstance([MockTestData.ARTICLE_VIEW_1, MockTestData.ARTICLE_VIEW_2]))
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.articles[0].slug').isEqualTo(MockTestData.ARTICLE_VIEW_1.getSlug())
                .jsonPath('$.articles[0].title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.articles[0].description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.articles[0].body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.articles[0].tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
                .jsonPath('$.articles[0].favorited').isEqualTo(MockTestData.ARTICLE_VIEW_1.getFavorited())
                .jsonPath('$.articles[0].favoritesCount').isEqualTo(MockTestData.ARTICLE_VIEW_1.getFavoritesCount())
                .jsonPath('$.articles[0].author.username').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getUsername())
                .jsonPath('$.articles[0].author.bio').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getBio())
                .jsonPath('$.articles[0].author.image').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().getImage())
                .jsonPath('$.articles[0].author.following').isEqualTo(MockTestData.ARTICLE_VIEW_1.getAuthor().isFollowing())
                .jsonPath('$.articles[1].author.username').isEqualTo(MockTestData.ARTICLE_VIEW_2.getAuthor().getUsername())

        where:
        uQStr                    || uOffset | uLimit
        '''?offset=2&limit=10''' || 2       | 10
        '''?limit=11'''          || 0       | 11
        '''?offset=5'''          || 5       | 20
        ""                       || 0       | 20
    }

    def "test GetArticle success"() {
        given: 'the slug of an article'
        String articleSlug = "articleSlug"

        when: 'calls api to retrieve the article'
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath + "{slug}"), articleHandler::getArticle))
                .configureClient().build()
        var result = client.get().uri(testingPath + articleSlug).exchange()

        then: 'api calls the  articale service getArticle for current user if the article is available'
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.getArticle(articleSlug, Optional.of(MockTestData.CURRENT_USER))
                >> Mono.just(MockTestData.ARTICLE_VIEW_1)

        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.article.slug').isEqualTo(MockTestData.ARTICLE_VIEW_1.getSlug())
                .jsonPath('$.article.title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.article.description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.article.body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.article.tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
    }

    def "test UpdateArticle success"() {
        given: 'the payload of the update'
        String jsonStr = """{
          "article": {
            "title": "Did you train your dragon?"
          }
        }"""
        String articleSlug = "articleSlug"

        when: 'calls api to update the article'
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(PUT(testingPath + "{slug}"), articleHandler::updateArticle))
                .configureClient().build()
        var result = client.put().uri(testingPath + articleSlug).bodyValue(jsonStr).exchange()

        then: 'api calles the updateArticle of articleService'
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.updateArticle(articleSlug, _, MockTestData.CURRENT_USER)
                >> Mono.just(MockTestData.ARTICLE_VIEW_1)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.article.slug').isEqualTo(MockTestData.ARTICLE_VIEW_1.getSlug())
                .jsonPath('$.article.title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.article.description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.article.body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.article.tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
    }

    def "test DeleteArticle success"() {
        given: ''
        String articleSlug = "articleSlug"

        when: ''
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(DELETE(testingPath + "{slug}"), articleHandler::deleteArticle))
                .configureClient().build()
        var result = client.delete().uri(testingPath + articleSlug).exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.deleteArticle(articleSlug, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.ARTICLE_1)
        result.expectStatus().isOk()
    }

    def "test GetComments success"() {
        given: ''
        String articleSlug = "articleSlug"

        when: ''
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath + "{slug}/comments"), articleHandler::getComments))
                .configureClient().build()
        var result = client.get().uri(testingPath + articleSlug + "/comments").exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.getComments(articleSlug, Optional.of(MockTestData.CURRENT_USER)) >>
                Mono.just(MultipleCommentsView.makeInstance([MockTestData.COMMENT_VIEW_11, MockTestData.COMMENT_VIEW_12]))
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.comments[0].id').isEqualTo(MockTestData.COMMENT_11.getId())
                .jsonPath('$.comments[0].body').isEqualTo(MockTestData.COMMENT_11.getBody())
                .jsonPath('$.comments[0].author.username').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getUsername())
                .jsonPath('$.comments[0].author.bio').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getBio())
                .jsonPath('$.comments[0].author.image').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getImage())
                .jsonPath('$.comments[0].author.following').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().isFollowing())
                .jsonPath('$.comments[1].author.username').isEqualTo(MockTestData.COMMENT_VIEW_12.getAuthor().getUsername())
    }

    def "test AddComment success"() {
        given: ''
        String jsonStr = """{
          "comment": {
            "body": "His name was my name too."
          }
        }"""
        String articleSlug = "articleSlug"

        when: ''
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath + "{slug}/comments"), articleHandler::addComment))
                .configureClient().build()
        var result = client.post().uri(testingPath + articleSlug + "/comments").bodyValue(jsonStr).exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.addComment(articleSlug, _, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.COMMENT_VIEW_11)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.comment.id').isEqualTo(MockTestData.COMMENT_VIEW_11.getId())
                .jsonPath('$.comment.body').isEqualTo(MockTestData.COMMENT_VIEW_11.getBody())
                .jsonPath('$.comment.author.username').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getUsername())
                .jsonPath('$.comment.author.bio').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getBio())
                .jsonPath('$.comment.author.image').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().getImage())
                .jsonPath('$.comment.author.following').isEqualTo(MockTestData.COMMENT_VIEW_11.getAuthor().isFollowing())
    }

    def "test DeleteComment success"() {
        given: ''
        String articleSlug = "articleSlug"
        String commentId = "commontId"

        when: ''
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(DELETE(testingPath + "{slug}/comments/{commentId}"), articleHandler::deleteComment))
                .configureClient().build()
        var result = client.delete().uri(testingPath + articleSlug + "/comments/" + commentId).exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        1 * articleService.deleteComment(commentId, articleSlug, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.COMMENT_11)
        result.expectStatus().isOk()
    }

    def "test FavoriteArticle success"() {
        given: 'the slug of an article'
        String articleSlug = "articleSlug"

        when: 'calls api to favorite the article'
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath + "{slug}/favorite"), articleHandler::favoriteArticle))
                .configureClient().build()
        var result = client.post().uri(testingPath + articleSlug + "/favorite").exchange()

        then: 'api calls favoriteArticle of article service to set the article favorited'
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        MockTestData.ARTICLE_VIEW_1.setFavorited(true)
        1 * articleService.favoriteArticle(articleSlug, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.ARTICLE_VIEW_1)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.article.title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.article.description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.article.body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.article.tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
                .jsonPath('$.article.favorited').isEqualTo(true)
    }

    def "test UnfavoriteArticle success"() {
        given: ''
        String articleSlug = "articleSlug"

        when: ''
        String testingPath = PATH_PREFIX + "/articles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(DELETE(testingPath + "{slug}/favorite"), articleHandler::unfavoriteArticle))
                .configureClient().build()
        var result = client.delete().uri(testingPath + articleSlug + "/favorite").exchange()

        then: ''
        1 * userSessionProvider.getCurrentUserOrEmpty() >> Mono.just(MockTestData.CURRENT_USER)
        MockTestData.ARTICLE_VIEW_1.setFavorited(false)
        1 * articleService.unfavoriteArticle(articleSlug, MockTestData.CURRENT_USER) >> Mono.just(MockTestData.ARTICLE_VIEW_1)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.article.title').isEqualTo(MockTestData.ARTICLE_VIEW_1.getTitle())
                .jsonPath('$.article.description').isEqualTo(MockTestData.ARTICLE_VIEW_1.getDescription())
                .jsonPath('$.article.body').isEqualTo(MockTestData.ARTICLE_VIEW_1.getBody())
                .jsonPath('$.article.tagList').isEqualTo(new ArrayList(MockTestData.ARTICLE_VIEW_1.getTags()))
//                .jsonPath('$.article.favorited').isEqualTo(false)
    }

    def "test retrieve all the used tags"() {
        when: 'call the api for the current user'
        String testingPath = PATH_PREFIX + "/tags"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath), articleHandler::getTags))
                .configureClient().build()
        var result = client.get().uri(testingPath).exchange()

        then: 'onc call to the current user session and the user in the response'
        1 * articleService.getTags()
                >> Mono.just(TagListView.makeInstance([new Tag("1", "tag1"), new Tag("2", "tag2"), new Tag("3", "tag3")]))
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.tags').value(v -> {
            if (!(v instanceof List)) return false
            List lv =  (List)v
            if (lv.size() != 3) return false
            return lv.get(0) == 'tag1' && lv.get(2) == 'tag3'
        })
    }
}
