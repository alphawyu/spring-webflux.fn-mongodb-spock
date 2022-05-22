package com.realworld.webfluxfn.service.user;

import com.realworld.webfluxfn.dto.request.UpdateUserRequest;
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest;
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest;
import com.realworld.webfluxfn.dto.view.UserView;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.exception.InvalidRequestException;
import com.realworld.webfluxfn.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
class SecuredUserService {
    private static final String ALREADY_IN_USE = "already in use";

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserTokenProvider tokenProvider;

    public Mono<UserView> login(final Mono<UserAuthenticationRequest> request) {
        return request.flatMap(m -> {
            final var email = m.getEmail();
            final var password = m.getPassword();
            return userRepository.findByEmailOrFail(email)
                .flatMap(user -> {
                    final String encodedPassword = user.getEncodedPassword();
                    return passwordService.matchesRowPasswordWithEncodedPassword(password, encodedPassword)
                            ? Mono.just(user)
                            : Mono.error(new InvalidRequestException("Password", "invalid"));
                }).map(this::createAuthenticationResponse);
        });
    }

    public Mono<UserView> signup(final UserRegistrationRequest request) {
        final User rawUser = request.toRawUser();
        return this.applyUsernameFromProvided(request.getUsername(), rawUser, true)
                .flatMap(u-> this.applyEmailFromProvided(request.getEmail(), u, true))
                .flatMap(u -> {
                    final String encodedPassword = passwordService.encodePassword(request.getPassword());
                    final String id = UUID.randomUUID().toString();
                    final User userToCreate = request.toUser(encodedPassword, id);
                    return userRepository.save(userToCreate)
                            .map(this::createAuthenticationResponse);
                });
    }

    public Mono<User> updateUser(final Mono<UpdateUserRequest> request, final User userToUpdate) {
        return request.flatMap(m -> {
            ofNullable(m.getBio()).ifPresent(userToUpdate::setBio);
            ofNullable(m.getImage()).ifPresent(userToUpdate::setImage);
            ofNullable(m.getPassword())
                    .ifPresent(password -> userToUpdate.setEncodedPassword(passwordService.encodePassword(password)));
            return applyUsernameFromProvided(m.getUsername(), userToUpdate, false)
                    .flatMap(u -> applyEmailFromProvided(m.getEmail(), u, false));
        }).flatMap(userRepository::save);
    }

    /* default */ Mono<User> applyUsernameFromProvided(final String userName, final User user, final boolean isSignUp) {
        if (StringUtils.isEmpty(userName) && isSignUp) {
            return Mono.error(new InvalidRequestException("Username", "username is required for signup"));
        }
        if (user.getUsername().equals(userName) && ! isSignUp) {
            // existing user can skip the user name check if not changed
            return Mono.just(user);
        }
        return userRepository.existsByUsername(userName)
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new InvalidRequestException("Username", ALREADY_IN_USE));
                    }
                    user.setUsername(userName);
                    return Mono.just(user);
                });
    }

    /* default */ Mono<User> applyEmailFromProvided(final String email, final User user, final boolean isSignUp) {
        if (StringUtils.isEmpty(email) && isSignUp) {
            return Mono.error(new InvalidRequestException("email", "email is required for signup"));
        }
        if (user.getEmail().equals(email) && ! isSignUp) {
            // existing user can skip the email check if not changed
            return Mono.just(user);
        }
        return userRepository.existsByEmail(email)
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new InvalidRequestException("Email", ALREADY_IN_USE));
                    }
                    user.setEmail(email);
                    return Mono.just(user);
                });
    }

    private UserView createAuthenticationResponse(final User user) {
        final var token = tokenProvider.getToken(user.getId());
        return UserView.fromUserAndToken(user, token);
    }
}
