package com.realworld.webfluxfn.api;

import com.realworld.testharness.article.ArticleApiTestClient;
import com.realworld.testharness.article.ArticleSamples;
import com.realworld.testharness.article.FindArticlesRequest;
import com.realworld.testharness.user.UserApiTestClient;
import com.realworld.testharness.user.UserSamples;
import com.realworld.webfluxfn.dto.view.ArticleView;
import com.realworld.webfluxfn.dto.request.CreateArticleRequest;
import com.realworld.webfluxfn.dto.request.CreateCommentRequest;
import com.realworld.webfluxfn.dto.request.UpdateArticleRequest;
import com.realworld.webfluxfn.dto.view.ProfileView;
import com.realworld.webfluxfn.dto.view.UserView;
import com.realworld.webfluxfn.persistence.repository.ArticleRepository;
import com.realworld.webfluxfn.persistence.repository.TagRepository;
import com.realworld.webfluxfn.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleApiTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TagRepository tagRepository;

    UserApiTestClient userApiTestClient;

    ArticleApiTestClient articleApiTestClient;

    @BeforeEach
    void setUp() {
        userApiTestClient = new UserApiTestClient(webTestClient);
        articleApiTestClient = new ArticleApiTestClient(webTestClient);
        userRepository.deleteAll().block();
        articleRepository.deleteAll().block();
        tagRepository.deleteAll().block();
    }

    @Test
    void shouldCreateArticle() {
        var user = userApiTestClient.signup();
        var createArticleRequest = ArticleSamples.sampleCreateArticleRequest();

        var result = articleApiTestClient.createArticle(createArticleRequest, user.getToken());
        assert result != null;
        var author = result.getAuthor();

        assertThatCreatedArticleIsRight(createArticleRequest, result);
        assertThatCreatedArticleHasRightAuthor(user, author);
        var savedArticles = articleRepository.findAll().collectList().block();
        assertThat(savedArticles).hasSize(1);
    }

    @Test
    void shouldFindArticles() {
        var expectedTag = "tag";
        var preparation = create2UsersAnd3Articles(expectedTag);

        var findArticlesRequest1 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.users.get(0).getUsername());
        var findArticlesRequest2 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.users.get(1).getUsername());

        // Test 1
        var articles1 = articleApiTestClient.findArticles(findArticlesRequest1).getResponseBody();
        var articles2 = articleApiTestClient.findArticles(findArticlesRequest2).getResponseBody();

        assert articles1 != null;
        assertThat(articles1.getArticlesCount()).isEqualTo(1);
        var article1 = articles1.getArticles().get(0);
        assertThat(article1)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.articles.get(0));

        assert articles2 != null;
        assertThat(articles2.getArticlesCount()).isEqualTo(1);
        var article2 = articles2.getArticles().get(0);
        assertThat(article2)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.articles.get(1));
    }

    @Test
    void shouldReturnFeed() {
        var follower = userApiTestClient.signup();
        var followingUserRR = UserSamples.sampleUserRegistrationRequest()
                .setUsername("following username")
                .setEmail("following@gmail.com");
        var followingUser = userApiTestClient.signup(followingUserRR);
        assert followingUser != null;
        userApiTestClient.follow(followingUser.getUsername(), follower.getToken());
        articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), follower.getToken());

        var resultBody = articleApiTestClient.feed(follower.getToken(), 1, 2).getResponseBody();

        assert resultBody != null;
        assertThat(resultBody.getArticlesCount()).isEqualTo(2);
        var hasRightAuthor = resultBody.getArticles().stream()
                .map(ArticleView::getAuthor)
                .allMatch(it -> it.getUsername().equals(followingUser.getUsername()));
        assertThat(hasRightAuthor).isTrue();
    }

    @Test
    void shouldReturnArticle() {
        var user = userApiTestClient.signup();
        var expected = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest().setTitle("article title"), user.getToken());
        assert expected != null;

        var actual = articleApiTestClient.getArticle("article-title", user.getToken());
        assert actual != null;

        assertThat(actual.getSlug()).isEqualTo(expected.getSlug());
    }

    @Test
    void shouldUpdateArticle() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var slug = article.getSlug();
        var updateArticleRequest = UpdateArticleRequest.builder()
                .body("new body")
                .description("new description")
                .title("new title").build();

        var updatedArticle = articleApiTestClient.updateArticle(slug, updateArticleRequest, user.getToken());
        assert updatedArticle != null;

        assertThat(updatedArticle.getAuthor()).isEqualTo(article.getAuthor());
        assertThat(updatedArticle.getBody()).isEqualTo(updateArticleRequest.getBody());
        assertThat(updatedArticle.getDescription()).isEqualTo(updateArticleRequest.getDescription());
        assertThat(updatedArticle.getTitle()).isEqualTo(updateArticleRequest.getTitle());
    }

    @Test
    void shouldDeleteArticle() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var slug = article.getSlug();

        articleApiTestClient.deleteArticle(slug, user.getToken());

        var articlesCount = articleRepository.count().block();
        assertThat(articlesCount).isZero();
    }

    @Test
    void shouldAddComment() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");

        var commentView = articleApiTestClient.addComment(article.getSlug(), request, user.getToken());
        assert commentView != null;

        assertThat(commentView.getBody()).isEqualTo(request.getBody());
        assertThat(commentView.getAuthor().getUsername()).isEqualTo(user.getUsername());
        var savedArticle = articleRepository.findAll().blockFirst();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isNotEmpty();
    }

    @Test
    void shouldDeleteComment() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");
        var commentView = articleApiTestClient.addComment(article.getSlug(), request, user.getToken());
        assert commentView != null;

        articleApiTestClient.deleteComment(article.getSlug(), commentView.getId(), user.getToken());

        var savedArticle = articleRepository.findAll().blockFirst();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isEmpty();
    }

    @Test
    void shouldGetComments() {
        var user = userApiTestClient.signup();
        userApiTestClient.follow(user.getUsername(), user.getToken());
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var comment1 = articleApiTestClient.addComment(article.getSlug(), "comment 1", user.getToken());
        var comment2 = articleApiTestClient.addComment(article.getSlug(), "comment 2", user.getToken());
        var expectedComments = Set.of(comment1, comment2);

        var actualComments = articleApiTestClient.getComments(article.getSlug(), user.getToken()).getResponseBody();

        assertThat(new HashSet<>(actualComments.getComments())).isEqualTo(expectedComments);
    }

    @Test
    void shouldFavoriteArticle() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var favoritedArticle = articleApiTestClient.favoriteArticle(article.getSlug(), user);
        assertThat(article.getFavorited()).isFalse();
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(favoritedArticle.getFavoritesCount()).isEqualTo(1);
    }

    @Test
    void shouldUnfavoriteArticle() {
        var user = userApiTestClient.signup();
        var article = articleApiTestClient.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var favoritedArticle = articleApiTestClient.favoriteArticle(article.getSlug(), user);
        var unfavoritedArticle = articleApiTestClient.unfavoriteArticle(article.getSlug(), user);
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(unfavoritedArticle.getFavorited()).isFalse();
    }

    @Test
    void shouldGetTags() {
        var user = userApiTestClient.signup();
        var request1 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of("tag1", "tag2", "tag2"));
        var request2 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of("tag3", "tag4", "tag3"));
        articleApiTestClient.createArticle(request1, user.getToken());
        articleApiTestClient.createArticle(request2, user.getToken());
        var tagListView = articleApiTestClient.getTags().getResponseBody();
        Set<String> tags = new HashSet<>(tagListView.getTags());
        assertThat(tags).isEqualTo(Set.of("tag1", "tag2", "tag3", "tag4"));
    }

    ArticlesAndUsers create2UsersAnd3Articles(String tag) {
        var user1 = userApiTestClient.signup();
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setUsername("test user 2")
                .setEmail("testemail2@gmail.com");
        var user2 = userApiTestClient.signup(userRegistrationRequest);
        assert user2 != null;

        var createArticleRequest1 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest2 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest3 = ArticleSamples.sampleCreateArticleRequest();

        var article1 = articleApiTestClient.createArticle(createArticleRequest1, user1.getToken());
        var article2 = articleApiTestClient.createArticle(createArticleRequest2, user2.getToken());
        articleApiTestClient.createArticle(createArticleRequest3, user2.getToken());
        assert article1 != null;
        assert article2 != null;
        return new ArticlesAndUsers(List.of(article1, article2), List.of(user1, user2));
    }

    private void assertThatCreatedArticleHasRightAuthor(UserView user, ProfileView author) {
        assertThat(author.getUsername()).isEqualTo(user.getUsername());
        assertThat(author.getBio()).isEqualTo(user.getBio());
        assertThat(author.getImage()).isEqualTo(user.getImage());
        assertThat(author.isFollowing()).isFalse();
    }

    private void assertThatCreatedArticleIsRight(CreateArticleRequest createArticleRequest, ArticleView result) {
        assertThat(result.getBody()).isEqualTo(createArticleRequest.getBody());
        assertThat(result.getDescription()).isEqualTo(createArticleRequest.getDescription());
        assertThat(result.getTitle()).isEqualTo(createArticleRequest.getTitle());
        assertThat(result.getTags()).isEqualTo(createArticleRequest.getTagList());
    }

    record ArticlesAndUsers(List<ArticleView> articles, List<UserView> users) {
    }
}
