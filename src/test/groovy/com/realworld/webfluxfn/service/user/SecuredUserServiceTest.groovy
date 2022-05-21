package com.realworld.webfluxfn.service.user

import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.dto.request.UpdateUserRequest
import com.realworld.webfluxfn.dto.request.UserAuthenticationRequest
import com.realworld.webfluxfn.dto.request.UserRegistrationRequest
import com.realworld.webfluxfn.dto.view.UserView
import com.realworld.webfluxfn.exception.InvalidRequestException
import com.realworld.webfluxfn.persistence.entity.User
import com.realworld.webfluxfn.persistence.repository.UserRepository
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import static junit.framework.TestCase.assertTrue
import static junit.framework.TestCase.assertEquals

class SecuredUserServiceTest extends Specification {
    private final UserRepository userRepository = Mock()
    private final PasswordService passwordService = Mock()
    private final UserTokenProvider tokenProvider = Mock()

    private final SecuredUserService securedUserService = new SecuredUserService(userRepository, passwordService, tokenProvider)

    void setup() {
    }

    void cleanup() {
    }

    def "test Login success"() {
        given: "a userLoginRequest"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        UserAuthenticationRequest userAuthenticationRequest = UserAuthenticationRequest.builder()
                .email(expectedUserView.email).password("password").build()
        Mono<UserAuthenticationRequest> userAuthenticationRequestMono = Mono.just(userAuthenticationRequest)
        and:
        userRepository.findByEmailOrFail(userAuthenticationRequest.getEmail()) >> Mono.just(MockTestData.AUTHOR_USER)
        passwordService.matchesRowPasswordWithEncodedPassword(
                userAuthenticationRequest.getPassword(), "author_password") >> true
        tokenProvider.getToken(MockTestData.AUTHOR_USER.getId()) >> expectedUserView.getToken()

        when: "the service calls to login"
        Mono<UserView> rtn = securedUserService.login(userAuthenticationRequestMono)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .assertNext(r-> {
                    assertEquals(expectedUserView, r)
                })
                .expectComplete().verify()
    }

    def "test Login failed on unmatched username password"() {
        given: "a userLoginRequest"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        UserAuthenticationRequest userAuthenticationRequest = UserAuthenticationRequest.builder()
                .email(expectedUserView.email).password("password").build()
        Mono<UserAuthenticationRequest> userAuthenticationRequestMono = Mono.just(userAuthenticationRequest)
        and:
        userRepository.findByEmailOrFail(userAuthenticationRequest.getEmail()) >> Mono.just(MockTestData.AUTHOR_USER)
        passwordService.matchesRowPasswordWithEncodedPassword(
                userAuthenticationRequest.getPassword(), "author_password") >> false

        when: "the service calls to login"
        Mono<UserView> rtn = securedUserService.login(userAuthenticationRequestMono)

        then: "the service returns following"
        StepVerifier.create(rtn)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof InvalidRequestException)
                }).verify()
    }

    def "test Signup Success"() {
        given: "a userRegisterRequest for the signup"
        UserView expectedUserView = MockTestData.AUTHOR_USER_VIEW
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .username(expectedUserView.getUsername()).email(expectedUserView.email).password("password").build()
        and:
        userRepository.existsByUsername(userRegistrationRequest.getUsername()) >> Mono.just(false)
        userRepository.existsByEmail(userRegistrationRequest.getEmail()) >> Mono.just(false)
        passwordService.encodePassword(userRegistrationRequest.getPassword()) >> "author_password"
        userRepository.save(_) >> Mono.just(MockTestData.AUTHOR_USER)
        tokenProvider.getToken(MockTestData.AUTHOR_USER.getId()) >> expectedUserView.getToken()

        when: "the service calls to signup the user"
        Mono<UserView> rtn = securedUserService.signup(userRegistrationRequest)

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
        UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
                .username(expectedUserView.getUsername()).email(expectedUserView.email).password("password")
                .bio(expectedUserView.getBio()).image(expectedUserView.getImage()).build()
        Mono<UpdateUserRequest> updateUserRequestMono = Mono.just(updateUserRequest)
        UserSessionProvider.UserSession currentUserSession = MockTestData.CURRENT_USER_SESSION
        User currentUser = MockTestData.CURRENT_USER
        and:
        securedUserService.updateUser(updateUserRequest, currentUser) >> Mono.just(MockTestData.AUTHOR_USER)
        userRepository.existsByUsername(updateUserRequest.getUsername()) >> Mono.just(false)
        userRepository.existsByEmail(updateUserRequest.getEmail()) >> Mono.just(false)
        passwordService.encodePassword(updateUserRequest.getPassword()) >> "author_password"
        userRepository.save(_) >> Mono.just(MockTestData.AUTHOR_USER)

        when: "the service calls to update user"
        Mono<User> rtn = securedUserService.updateUser(updateUserRequestMono, currentUser)

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

    def "test setUsernameFromProvided "() {
        given: "a user"
        User user = MockTestData.makeUser("xx");
        user.setUsername("currentUserName")
        boolean condUserNameUsed = uUserNameUsed
        and:
        userRepository.existsByUsername(uUserName) >> Mono.just(condUserNameUsed)

        when: "the service calls determine the input username"
        Mono<User> rtn = securedUserService.applyUsernameFromProvided(uUserName, user, uIsSignUp)

        then: "the service returns following"
        if (uExpectError) {
            StepVerifier.create(rtn)
                    .expectError().verify()
        } else {
            StepVerifier.create(rtn)
                    .assertNext(r-> {
                    })
                    .expectComplete().verify()
        }

        where:
        uUserName           | uUserNameUsed | uIsSignUp || uExpectError
        "currentUserName"   | true          | true      || true
        "currentUserName"   | true          | false     || false
        "differentUserName" | true          | true      || true
        "differentUserName" | true          | false     || true
        "differentUserName" | false         | true      || false
        "differentUserName" | false         | false     || false
        ""                  | false         | true      || true
        ""                  | false         | false     || false
        null                | false         | true      || true
        null                | false         | false     || false

    }


    def "test setEmailFromProvided "() {
        given: "a user"
        User user = MockTestData.makeUser("xx");
        user.setEmail("current@Email")
        boolean condEmailUsed = uEmailUsed
        and:
        userRepository.existsByEmail(uEmail) >> Mono.just(condEmailUsed)

        when: "the service calls determine the input email"
        Mono<User> rtn = securedUserService.applyEmailFromProvided(uEmail, user, uIsSignUp)

        then: "the service returns following"
        if (uExpectError) {
            StepVerifier.create(rtn)
                    .expectError().verify()
        } else {
            StepVerifier.create(rtn)
                    .assertNext(r-> {
                    })
                    .expectComplete().verify()
        }

        where:
        uEmail            | uEmailUsed | uIsSignUp || uExpectError
        "current@Email"   | true       | true      || true
        "current@Email"   | true       | false     || false
        "different@Email" | true       | true      || true
        "different@Email" | true       | false     || true
        "different@Email" | false      | true      || false
        "different@Email" | false      | false     || false
        ""                | false      | true      || true
        ""                | false      | false     || false
        null              | false      | true      || true
        null              | false      | false     || false

    }
}
