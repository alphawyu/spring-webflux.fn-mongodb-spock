package com.realworld.webfluxfn.service.user;

import com.realworld.webfluxfn.dto.request.UpdateUserRequest;
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest;
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest;
import com.realworld.webfluxfn.dto.view.ProfileView;
import com.realworld.webfluxfn.dto.view.UserView;
import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final SecuredUserService securedUserService;
    private final UserRepository userRepository;

    public Mono<ProfileView> getProfile(final String profileUserName, final User viewerUser) {
        return userRepository.findByUsernameOrFail(profileUserName)
                .map(user -> ProfileView.convertToProfileViewByViewerUser(user, viewerUser));
    }

    public Mono<ProfileView> getProfile(final String profileUserName) {
        return userRepository.findByUsernameOrFail(profileUserName)
                .map(ProfileView::toUnfollowedProfileView);
    }

    public Mono<UserView> signup(final Mono<UserRegistrationRequest> request) {
        return request.flatMap(r -> securedUserService.signup(r));
    }

    public Mono<UserView> login(final Mono<UserAuthenticationRequest> request) {
        return securedUserService.login(request);
    }

    public Mono<UserView> updateUser(final Mono<UpdateUserRequest> request, final UserSessionProvider.UserSession userSession) {
        return Mono.defer(() ->securedUserService.updateUser(request, userSession.user()))
                .map(it -> UserView.fromUserAndToken(it, userSession.token()));
    }

    public Mono<ProfileView> follow(final String profileUserName, final User follower) {
        return userRepository.findByUsernameOrFail(profileUserName)
                .flatMap(userToFollow -> {
                    follower.follow(userToFollow);
                    return userRepository.save(follower).thenReturn(userToFollow);
                })
                .map(ProfileView::toFollowedProfileView);
    }

    public Mono<ProfileView> unfollow(final String profileUserName, final User follower) {
        return userRepository.findByUsernameOrFail(profileUserName)
                .flatMap(userToUnfollow -> {
                    follower.unfollow(userToUnfollow);
                    return userRepository.save(follower).thenReturn(userToUnfollow);
                })
                .map(ProfileView::toUnfollowedProfileView);
    }
}
