package com.realworld.webfluxfn

import com.realworld.webfluxfn.dto.view.ArticleView
import com.realworld.webfluxfn.dto.view.CommentView
import com.realworld.webfluxfn.dto.view.ProfileView
import com.realworld.webfluxfn.dto.view.UserView
import com.realworld.webfluxfn.persistence.entity.Article
import com.realworld.webfluxfn.persistence.entity.Comment
import com.realworld.webfluxfn.persistence.entity.Tag
import com.realworld.webfluxfn.persistence.entity.User
import com.realworld.webfluxfn.service.user.UserSessionProvider

import java.time.Instant

class MockTestData {
    static User makeUser(String prefix) {
        return User.builder()
                .encodedPassword(prefix + "_password")
                .email(prefix + "Email").username(prefix + "UserName")
                .bio(prefix + "UserBio").image(prefix + "UserImage")
                .id(prefix + "_id").followingIds(null)
                .build();
    }
    static UserView makeUserView(User baseUser, String prefix) {
        return UserView.builder()
                .email(baseUser.getEmail()).username(baseUser.getUsername())
                .bio(baseUser.getBio()).image(baseUser.getImage())
                .token(prefix+ "UserToken").build()
    }
    static ProfileView makeUserProfile(User baseUser) {
        return ProfileView.builder()
                .username(baseUser.getUsername()).bio(baseUser.getBio()).image(baseUser.getImage())
                .following(true).build()
    }

    static Article makeArticle(String suffix, User authorUser, List<String> tags, List<Comment> comments, List<String> favoritingUserIds) {
        return new Article(
                "generatedSlug"+suffix, "title"+suffix,
                "description"+suffix,"body"+suffix,
                Instant.parse("2007-12-03T10:15:30.00Z"),
                Instant.parse("2007-12-03T10:15:30.01Z"),
                authorUser.getId(),
                tags, comments, favoritingUserIds)
    }
    static ArticleView makeArticleView(Article baseArticle, ProfileView authorUserProfile) {
        return ArticleView.builder()
                .slug(baseArticle.getSlug())
                .title(baseArticle.getTitle())
                .description(baseArticle.getDescription())
                .body(baseArticle.getBody())
                .tags(baseArticle.getTags())
                .createdAt(baseArticle.getCreatedAt())
                .updatedAt(baseArticle.getUpdatedAt())
                .favorited(true)
                .favoritesCount(baseArticle.getFavoritesCount())
                .author(authorUserProfile).build()
    }

    static Tag makeTag(String suffix) {
        return  Tag.builder().id("id" + suffix).tagName("tagName" + suffix).build()
    }

    static Comment makeCommnent(String suffix, User commentUser) {
        return Comment.builder()
                .id("generatedCommentId"+suffix).body("body"+suffix)
                .createdAt(Instant.parse("2007-12-03T10:15:30.00Z"))
                .updatedAt(Instant.parse("2007-12-03T10:15:30.01Z"))
                .authorId(commentUser.getId()).build();;
    }
    static CommentView makeCommnentView(Comment baseComment, ProfileView userProfile) {
        return CommentView.builder()
                .id(baseComment.getId()).body(baseComment.getBody())
                .createdAt(Instant.parse("2007-12-03T10:15:30.00Z"))
                .updatedAt(Instant.parse("2007-12-03T10:15:30.01Z"))
                .author(userProfile).build();
    }

    public static final User AUTHOR_USER = makeUser("author")
    public static final UserView AUTHOR_USER_VIEW = makeUserView(AUTHOR_USER, "author")
    public static final ProfileView AUTHOR_USER_PROFILE = makeUserProfile(AUTHOR_USER)

    public static final User CURRENT_USER = makeUser("current")
    public static final UserView CURRENT_USER_VIEW = makeUserView(CURRENT_USER, "current")
    public static final ProfileView CURRENT_USER_PROFILE = makeUserProfile(CURRENT_USER)

    public static final Article ARTICLE_1 = makeArticle("1", AUTHOR_USER, ["tag11", "tag12", "tag13"], null, null)
    public static final Article ARTICLE_2 = makeArticle("2", AUTHOR_USER, ["tag21", "tag22", "tag23"], null, null)

    public static final ArticleView ARTICLE_VIEW_1 = makeArticleView(ARTICLE_1, AUTHOR_USER_PROFILE)
    public static final ArticleView ARTICLE_VIEW_2 = makeArticleView(ARTICLE_2, AUTHOR_USER_PROFILE)

    public static final UserSessionProvider.UserSession CURRENT_USER_SESSION
            = new UserSessionProvider.UserSession(CURRENT_USER, "current_token")

    public static Tag TAG_1 = makeTag("1")
    public static Tag TAG_2 = makeTag("2")
    public static Tag TAG_3 = makeTag("3")

    public static final Comment COMMENT_11 = makeCommnent("11", CURRENT_USER);
    public static final Comment COMMENT_12 = makeCommnent("12", CURRENT_USER);
    public static final Comment COMMENT_21 = makeCommnent("21", CURRENT_USER);

    public static final CommentView COMMENT_VIEW_11 = makeCommnentView(COMMENT_11, CURRENT_USER_PROFILE);
    public static final CommentView COMMENT_VIEW_12 = makeCommnentView(COMMENT_12, CURRENT_USER_PROFILE);
    public static final CommentView COMMENT_VIEW_21 = makeCommnentView(COMMENT_21, CURRENT_USER_PROFILE);

}
