package com.realworld.webfluxfn.service.user

import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.dto.request.UpdateUserRequest
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest
import com.realworld.webfluxfn.dto.view.ProfileView
import com.realworld.webfluxfn.dto.view.UserView
import com.realworld.webfluxfn.persistence.repository.UserRepository
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import com.realworld.webfluxfn.persistence.entity.User

import static junit.framework.TestCase.assertEquals

class UserServiceTest extends Specification {
    private final SecuredUserService securedUserService = Mock()
    private final UserRepository userRepository = Mock()
    private final UserService userService = new UserService(securedUserService, userRepository)

    void setup() {
    }

    void cleanup() {
    }

    def "test GetProfile with profile user name, and viewer user success"() {
        given: "user profile name, view user"
        ProfileView expectedProfile = MockTestData.makeUserProfile(MockTestData.AUTHOR_USER)
        expectedProfile.setFollowing(false)
        String profileUserName = expectedProfile.getUsername()
        User viewUser = MockTestData.CURRENT_USER
        and:
        userRepository.findByUsernameOrFail (profileUserName) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service retrieves user profile"
        Mono<ProfileView> rtn = userService.getProfile(profileUserName, viewUser)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedProfile, r)
                })
                .expectComplete().verify()
    }

    def "test GetProfile with profile user name success"() {
        given: "user profile name"
        ProfileView expectedProfile = MockTestData.AUTHOR_USER_PROFILE
        String profileUserName = expectedProfile.getUsername()
        and:
        userRepository.findByUsernameOrFail (profileUserName) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service retrieves user profile"
        Mono<ProfileView> rtn = userService.getProfile(profileUserName)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedProfile.getUsername(), r.getUsername())
                    assertEquals(expectedProfile.getBio(), r.getBio())
                    assertEquals(expectedProfile.getImage(), r.getImage())
                    assertEquals(false, r.isFollowing())
                })
                .expectComplete().verify()
    }

    def "test Signup success"() {
        given: "a userRegisterRequest for the signup"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .username(expectedUserView.getUsername()).email(expectedUserView.email).password("password").build()
        and:
        securedUserService.signup(userRegistrationRequest) >> Mono.just(expectedUserView)

        when: "the service calls to signup the user"
        Mono<UserView> rtn = userService.signup(Mono.just(userRegistrationRequest))

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedUserView, r)
                })
                .expectComplete().verify()
    }

    def "test Login success"() {
        given: "a userLoginRequest"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        Mono<UserAuthenticationRequest> userAuthenticationRequest = Mono.just(UserAuthenticationRequest.builder()
                .email(expectedUserView.email).password("password").build())
        and:
        securedUserService.login(userAuthenticationRequest) >> Mono.just(expectedUserView)

        when: "the service calls to login"
        Mono<UserView> rtn = userService.login(userAuthenticationRequest)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedUserView, r)
                })
                .expectComplete().verify()
    }

    def "test UpdateUser success"() {
        given: "a updateUserRequest"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        Mono<UpdateUserRequest> updateUserRequest = Mono.just(UpdateUserRequest.builder()
                .username(expectedUserView.getUsername()).email(expectedUserView.email).password("password")
                .bio(expectedUserView.getBio()).image(expectedUserView.getImage()).build())
        UserSessionProvider.UserSession currentUserSession = MockTestData.CURRENT_USER_SESSION
        User currentUser = MockTestData.CURRENT_USER
        and:
        securedUserService.updateUser(updateUserRequest, currentUser) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service calls to update user"
        Mono<UserView> rtn = userService.updateUser(updateUserRequest, currentUserSession)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedUserView.getEmail(), r.getEmail())
                    assertEquals(expectedUserView.getUsername(), r.getUsername())
                    assertEquals(expectedUserView.getBio(), r.getBio())
                    assertEquals(expectedUserView.getImage(), r.getImage())
                })
                .expectComplete().verify()
    }

    def "Follow"() {
        given: "following user name, and follower user"
        ProfileView expectedProfile = MockTestData.AUTHOR_USER_PROFILE
        String followingUserName = expectedProfile.getUsername()
        User followerUser = MockTestData.CURRENT_USER
        and:
        userRepository.findByUsernameOrFail (followingUserName) >> Mono.just(MockTestData.AUTHOR_USER)
        userRepository.save(_)  >> Mono.just(followerUser)

        when: "the service to setup following relation"
        Mono<ProfileView> rtn = userService.follow(followingUserName, followerUser)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedProfile, r)
                })
                .expectComplete().verify()
    }

    def "Unfollow"() {
        given: "following user name, and follower user"
        ProfileView expectedProfile = MockTestData.AUTHOR_USER_PROFILE
        String followingUserName = expectedProfile.getUsername()
        User followerUser = MockTestData.CURRENT_USER
        and:
        userRepository.findByUsernameOrFail(followingUserName) >> Mono.just(MockTestData.AUTHOR_USER)
        userRepository.save(_)  >> Mono.just(MockTestData.AUTHOR_USER) >> Mono.just(followerUser)

        when: "the service to remove following relation"
        Mono<ProfileView> rtn = userService.unfollow(followingUserName, followerUser)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedProfile.getUsername(), r.getUsername())
                    assertEquals(expectedProfile.getBio(), r.getBio())
                    assertEquals(expectedProfile.getImage(), r.getImage())
                    assertEquals(false, r.isFollowing())
                })
                .expectComplete().verify()
    }
}
