package com.realworld.webfluxfn.persistence.repository;

import com.realworld.webfluxfn.persistence.entity.Article;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.exception.InvalidRequestException;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByEmail(Mono<String> email);

    Mono<Boolean> existsByUsername(String username);

    Mono<User> findByUsername(String username);

    default Mono<User> findAuthorByArticle(final Article article) {
        return findById(article.getAuthorId());
    }

    default Mono<User> findByUsernameOrFail(final String username) {
        return findByUsername(username)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Username", "not found")));
    }

    default Mono<User> findByEmailOrFail(final String email) {
        return findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Email", "not found")));
    }
}
