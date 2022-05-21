package com.realworld.webfluxfn.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AppRouterFunction {

    @Bean
    public RouterFunction<ServerResponse> articleRoutes(final ArticleHandler articleHandler) {
        return nest(path("/api"),
                nest(path("/articles"),
                        route(DELETE("/{slug}/comments/{commentId}"), articleHandler::deleteComment)
                                .andRoute(GET("/{slug}/comments"), articleHandler::getComments)
                                .andRoute(POST("/{slug}/comments"), articleHandler::addComment)
                                .andRoute(POST("/{slug}/favorite"), articleHandler::favoriteArticle)
                                .andRoute(DELETE("/{slug}/favorite"), articleHandler::unfavoriteArticle)
                                .andRoute(GET("/feed"), articleHandler::feed)
                                .andRoute(GET("/{slug}"), articleHandler::getArticle)
                                .andRoute(PUT("/{slug}"), articleHandler::updateArticle)
                                .andRoute(DELETE("/{slug}"), articleHandler::deleteArticle)
                                .andRoute(method(HttpMethod.POST), articleHandler::createArticle)
                                .andRoute(method(HttpMethod.GET), articleHandler::getArticles)
                ).andRoute(GET("/tags"), articleHandler::getTags)
        );
    }

    @Bean
    public RouterFunction<ServerResponse> usersRoutes(final UserHandler userHandler) {
        return nest(path("/api"),
                nest(path("/users"),
                        route(POST("/login"), userHandler::login)
                                .andRoute(method(HttpMethod.POST), userHandler::signup)
                ).andNest(path("/user"),
                        route(method(HttpMethod.GET), userHandler::currentUser)
                                .andRoute(method(HttpMethod.PUT), userHandler::updateUser)
                ).andNest(path("/profiles"),
                        route(POST("/{username}/follow"), userHandler::follow)
                                .andRoute(DELETE("/{username}/follow"), userHandler::unfollow)
                                .andRoute(GET("/{username}"), userHandler::getProfile)
                )
        );
    }

}

