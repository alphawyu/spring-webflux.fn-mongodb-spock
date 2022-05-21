package com.realworld.webfluxfn.api;

import com.realworld.testharness.user.UserApiTestClient;
import com.realworld.testharness.user.UserSamples;
import com.realworld.webfluxfn.dto.request.UpdateUserRequest;
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest;
import com.realworld.webfluxfn.dto.view.UserView;
import com.realworld.webfluxfn.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserRepository userRepository;

    UserApiTestClient userApiTestClient;

    @BeforeEach
    void setUp() {
        userApiTestClient = new UserApiTestClient(webTestClient);
        userRepository.deleteAll().block();
    }

    @Test
    void shouldSignupUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();

        var result = userApiTestClient.signup(userRegistrationRequest);
        var body = requireNonNull(result);

        assertThatSignupResponseIsValid(userRegistrationRequest, body);
    }

    private void assertThatSignupResponseIsValid(UserRegistrationRequest userRegistrationRequest, UserView body) {
        assertThat(body.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(body.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(body.getBio()).isNull();
        assertThat(body.getImage()).isNull();
        assertThat(body.getToken()).isNotEmpty();
    }

    @Test
    void shouldLoginRegisteredUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        userApiTestClient.signup(userRegistrationRequest);

        var userAuthenticationRequest = UserSamples.sampleUserAuthenticationRequest();
        var result = userApiTestClient.login(userAuthenticationRequest);

        requireNonNull(result);
        assertThatLoginResponseIsValid(userRegistrationRequest, result);
    }

    private void assertThatLoginResponseIsValid(UserRegistrationRequest userRegistrationRequest, UserView result) {
        assertThat(result.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(result.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(result.getBio()).isNull();
        assertThat(result.getImage()).isNull();
        assertThat(result.getToken()).isNotEmpty();
    }

    @Test
    void shouldGetCurrentUser() {
        var response = userApiTestClient.signup(UserSamples.sampleUserRegistrationRequest());
        requireNonNull(response);

        var body = userApiTestClient.currentUser(response.getToken());

        requireNonNull(body);
        assertThat(body.getUsername()).isEqualTo(response.getUsername());
        assertThat(body.getEmail()).isEqualTo(response.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        var responseBody = userApiTestClient.signup(UserSamples.sampleUserRegistrationRequest());
        requireNonNull(responseBody);

        var updateUserRequest = UserSamples.sampleUpdateUserRequest();
        System.out.println(responseBody.getToken());
        var body = userApiTestClient.updateUser(responseBody.getToken(), updateUserRequest);

        requireNonNull(body);
        assertThatUserIsSavedAfterUpdate(updateUserRequest);
        assertThatUpdateUserResponseIsValid(updateUserRequest, body);
    }

    @Test
    void shouldReturnProfileByNameWhenUnauthorizedUser() {
        var request = UserSamples.sampleUserRegistrationRequest();
        userApiTestClient.signup(request);

        var result = userApiTestClient.getProfile(request.getUsername());
        var body = requireNonNull(result);

        assertThat(body.getUsername()).isEqualTo(request.getUsername());
        assertThat(body.isFollowing()).isFalse();
    }

    @Test
    void shouldFollowAndReturnRightProfile() {
        var followerRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var followeeRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setEmail("testemail2@gmail.com")
                .setUsername("testname2");
        userApiTestClient.signup(followeeRegistrationRequest);
        var follower = userApiTestClient.signup(followerRegistrationRequest);
        assert follower != null;
        var followeeUsername = followeeRegistrationRequest.getUsername();
        var followerAuthToken = follower.getToken();
        var followeeProfile = userApiTestClient.follow(followeeUsername, followerAuthToken);

        var profileDto = userApiTestClient.getProfile(followeeUsername, followerAuthToken);

        assert profileDto != null;
        assertThat(profileDto.getUsername()).isEqualTo(followeeUsername);
        assertThat(profileDto.isFollowing()).isTrue();
    }

    @Test
    void shouldFollowUser() {
        var followeeRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var followerRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setEmail("testemail2@gmail.com")
                .setUsername("testname2");
        userApiTestClient.signup(followeeRegistrationRequest);
        var followerDto = userApiTestClient.signup(followerRegistrationRequest);
        assert followerDto != null;
        var followeeUsername = followeeRegistrationRequest.getUsername();
        var authToken = followerDto.getToken();

        var profileDto = userApiTestClient.follow(followeeUsername, authToken);

        requireNonNull(profileDto);
        assertThat(profileDto.getUsername()).isEqualTo(followeeUsername);
        assertThat(profileDto.isFollowing()).isTrue();
        assertThatPersistedFollowerIsFollowed(followeeRegistrationRequest, followerRegistrationRequest);
    }

    @Test
    void shouldUnfollowUser() {
        var followeeRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var followerRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setEmail("testemail2@gmail.com")
                .setUsername("testname2");
        var followerDto = prepareFollowerAndFollowee(followeeRegistrationRequest, followerRegistrationRequest);
        var followeeUsername = followeeRegistrationRequest.getUsername();
        var authToken = followerDto.getToken();

        var body = userApiTestClient.unfollow(followeeUsername, authToken);

        assert body != null;
        assertThat(body.getUsername()).isEqualTo(followeeUsername);
        assertThat(body.isFollowing()).isFalse();
        assertThatPersistedFollowerIsUnfollowed(followeeRegistrationRequest, followerRegistrationRequest);
    }

    private void assertThatPersistedFollowerIsUnfollowed(UserRegistrationRequest followeeRegistrationRequest, UserRegistrationRequest followerRegistrationRequest) {
        var follower = userRepository.findByUsername(followerRegistrationRequest.getUsername()).block();
        var followee = userRepository.findByUsername(followeeRegistrationRequest.getUsername()).block();
        assert follower != null;
        assert followee != null;
        assertThat(follower.getFollowingIds()).doesNotContain(followee.getId());
    }

    private UserView prepareFollowerAndFollowee(UserRegistrationRequest followeeRegistrationRequest, UserRegistrationRequest followerRegistrationRequest) {
        userApiTestClient.signup(followeeRegistrationRequest);
        var followerDto = userApiTestClient.signup(followerRegistrationRequest);
        assert followerDto != null;
        var followeeUsername1 = followeeRegistrationRequest.getUsername();
        var authToken1 = followerDto.getToken();
        userApiTestClient.follow(followeeUsername1, authToken1);
        return followerDto;
    }

    private void assertThatPersistedFollowerIsFollowed(UserRegistrationRequest followeeRequest, UserRegistrationRequest followerRequest) {
        var follower = userRepository.findByUsername(followerRequest.getUsername()).block();
        var followee = userRepository.findByUsername(followeeRequest.getUsername()).block();
        assert follower != null;
        assert followee != null;
        assertThat(follower.getFollowingIds()).contains(followee.getId());
    }

    private void assertThatUserIsSavedAfterUpdate(UpdateUserRequest updateUserRequest) {
        var user = requireNonNull(userRepository.findByEmail(updateUserRequest.getEmail()).block());
        assertThat(user.getUsername()).isEqualTo(updateUserRequest.getUsername());
        assertThat(user.getBio()).isEqualTo(updateUserRequest.getBio());
        assertThat(user.getEmail()).isEqualTo(updateUserRequest.getEmail());
        assertThat(user.getImage()).isEqualTo(updateUserRequest.getImage());
    }

    private void assertThatUpdateUserResponseIsValid(UpdateUserRequest updateUserRequest, UserView body) {
        assertThat(body.getBio()).isEqualTo(updateUserRequest.getBio());
        assertThat(body.getImage()).isEqualTo(updateUserRequest.getImage());
        assertThat(body.getUsername()).isEqualTo(updateUserRequest.getUsername());
        assertThat(body.getEmail()).isEqualTo(updateUserRequest.getEmail());
    }
}