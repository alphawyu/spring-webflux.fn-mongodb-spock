package com.realworld.webfluxfn.api

import com.realworld.webfluxfn.MockTestData
import com.realworld.webfluxfn.api.UserHandler
import com.realworld.webfluxfn.dto.view.ProfileView
import com.realworld.webfluxfn.dto.request.UpdateUserRequest
import com.realworld.webfluxfn.dto.view.UserView
import com.realworld.webfluxfn.persistence.entity.User
import com.realworld.webfluxfn.service.user.UserSessionProvider
import com.realworld.webfluxfn.service.user.UserService
import org.spockframework.spring.SpringBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Title

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import static org.springframework.web.reactive.function.server.RequestPredicates.GET
import static org.springframework.web.reactive.function.server.RequestPredicates.POST
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT

//@WebFluxTest(UserHandler.class)
@Title("test User Handler api")
//@AutoConfigureWebFlux
class UserHandlerTest extends Specification {
    final static String PATH_PREFIX = '/api'
    @SpringBean
    private final UserService userService = Mock()
    @SpringBean
    private final UserSessionProvider userSessionProvider = Mock()

    @SpringBean
    private final UserHandler userHandler = new UserHandler(userService, userSessionProvider)

    void setup() {
    }

    void cleanup() {
    }

    def "test POST user Login success"() {
        given: 'an existing user {email, password}'
        String jsonString  = '''{'user': {'email':'testEmail", 'password':"testPassword"}}'''
        UserView currentUserView = MockTestData.CURRENT_USER_VIEW

        when: 'call the api to authenticate the login'
        String testingPath = PATH_PREFIX + "/users/login"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath), userHandler::login))
                .configureClient().build()
        var result = client.post().uri(testingPath).bodyValue(jsonString).exchange()

        then: 'the api calls credentialService.login to authenticate login and jwt token is in the response'
        1 * userService.login(_) >> Mono.just(currentUserView)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.user.email').isEqualTo(currentUserView.getEmail())
                .jsonPath('$.user.token').isEqualTo(currentUserView.getToken())
                .jsonPath('$.user.username').isEqualTo(currentUserView.getUsername())
                .jsonPath('$.user.bio').isEqualTo(currentUserView.getBio())
                .jsonPath('$.user.image').isEqualTo(currentUserView.getImage())
    }

    def "test POST register a user success"() {
        given: 'a non-existing user {username, password, email}'
        String jsonString  = '''{'user': {'username':"testUserName", 'email':'testEmail", 'password':"testPassword"}}'''
        UserView currentUserView = MockTestData.CURRENT_USER_VIEW

        when: 'call the api to register the user'
        String testingPath = PATH_PREFIX + "/users"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath), userHandler::signup))
                .configureClient().build()
        var result = client.post().uri(testingPath).bodyValue(jsonString).exchange()

        then: 'the api calls userService.signup to create the user and the user information is in the response'
        1 * userService.signup(_) >> Mono.just(currentUserView)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.user.email').isEqualTo(currentUserView.getEmail())
                .jsonPath('$.user.token').isEqualTo(currentUserView.getToken())
                .jsonPath('$.user.username').isEqualTo(currentUserView.getUsername())
                .jsonPath('$.user.bio').isEqualTo(currentUserView.getBio())
                .jsonPath('$.user.image').isEqualTo(currentUserView.getImage())
    }

    def "test GET currentUser success"() {
        given: 'a login user with valid token'
        String mockToken = "mockToken"
        User currentUser = MockTestData.CURRENT_USER

        when: 'call the api for the current user'
        String testingPath = PATH_PREFIX + "/user"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath), userHandler::currentUser))
                .configureClient().build()
        var result= client.get().uri(testingPath).exchange()

        then: 'onc call to the current user session and the user in the response'
        1 * userSessionProvider.getCurrentUserSessionOrEmpty()
                >> Mono.just(new UserSessionProvider.UserSession(currentUser, mockToken))
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.user.email').isEqualTo(currentUser.getEmail())
                .jsonPath('$.user.token').isEqualTo("mockToken")
                .jsonPath('$.user.username').isEqualTo(currentUser.getUsername())
                .jsonPath('$.user.bio').isEqualTo(currentUser.getBio())
                .jsonPath('$.user.image').isEqualTo(currentUser.getImage())
    }

    def "test PUT UpdateUser success"() {
        given: 'an existing user with updated bio and image'
        String jsonString = '''{"user":{"username":"meme", "email":"me@me.me",
                                "bio": "I like to skateboard",
                                "image": "https://i.stack.imgur.com/xHWG8.jpg"}}'''
        String currToken = "currToken"
        User currentUser = MockTestData.CURRENT_USER
        UserView currentUserView = MockTestData.CURRENT_USER_VIEW
        UserSessionProvider.UserSession currUserSession = new UserSessionProvider.UserSession(currentUser, currToken)


        when: 'call the api to update the user'
        String testingPath = PATH_PREFIX + "/user"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(PUT(testingPath), userHandler::updateUser))
                .configureClient().build()
        var result = client.put().uri(testingPath).bodyValue(jsonString).exchange()

        then: 'the api get user from current session, and user update request to update the user'
        1 * userSessionProvider.getCurrentUserSessionOrEmpty() >> Mono.just(currUserSession)
        1 * userService.updateUser(_ as Mono<UpdateUserRequest>, currUserSession)
                >> Mono.just(currentUserView)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.user.email').isEqualTo(currentUserView.getEmail())
                .jsonPath('$.user.token').isEqualTo(currentUserView.getToken())
                .jsonPath('$.user.username').isEqualTo(currentUserView.getUsername())
                .jsonPath('$.user.bio').isEqualTo(currentUser.getBio())
                .jsonPath('$.user.image').isEqualTo(currentUser.getImage())
    }

    def "test GET user Profile by profile user name"() {
        given: 'a existing user name'
        String profileUserName = "profileusername"
        User currentUser = MockTestData.CURRENT_USER
        ProfileView currentUserProfile = MockTestData.CURRENT_USER_PROFILE

        when: 'call the api to retrieve user profile'
        String testingPath = PATH_PREFIX + "/profiles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(GET(testingPath + "{username}"), userHandler::getProfile))
                .configureClient().build()
        var result= client.get().uri(testingPath + profileUserName).exchange()

        then: "the user is retrieved on current user's perspective in the response"
        1 * userSessionProvider.getCurrentUserOrEmpty()  >> Mono.just(currentUser)
        1 * userService.getProfile(profileUserName, currentUser) >> Mono.just(currentUserProfile)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.profile.username').isEqualTo(currentUserProfile.getUsername())
                .jsonPath('$.profile.bio').isEqualTo(currentUserProfile.getBio())
                .jsonPath('$.profile.image').isEqualTo(currentUserProfile.getImage())
                .jsonPath('$.profile.following').isEqualTo(currentUserProfile.isFollowing())
    }

    def "test Follow author success"() {
        given: 'an existing author (user)'
        String profileUserName = "authorname"
        User currentUser = MockTestData.CURRENT_USER
        ProfileView currentUserProfile = MockTestData.CURRENT_USER_PROFILE

        when: 'call the api to follow the user'
        String testingPath = PATH_PREFIX + "/profiles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(POST(testingPath + "{username}/follow"), userHandler::follow))
                .configureClient().build()
        var result= client.post().uri(testingPath + profileUserName + "/follow").exchange()

        then: 'the author is updated as followed and updated profile is in the response'
        1 * userSessionProvider.getCurrentUserOrEmpty()  >> Mono.just(currentUser)
        currentUserProfile.setFollowing(true)
        1 * userService.follow(profileUserName, currentUser) >> Mono.just(currentUserProfile)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.profile.username').isEqualTo(currentUserProfile.getUsername())
                .jsonPath('$.profile.bio').isEqualTo(currentUserProfile.getBio())
                .jsonPath('$.profile.image').isEqualTo(currentUserProfile.getImage())
                .jsonPath('$.profile.following').isEqualTo(true)
    }

    def "test Unfollow author success"() {
        given: 'an exising author that is following'
        String profileUserName = "authorname"
        User currentUser = MockTestData.CURRENT_USER
        ProfileView currentUserProfile = MockTestData.CURRENT_USER_PROFILE

        when: 'call the api to delete the following status'
        String testingPath = PATH_PREFIX + "/profiles/"
        var client = WebTestClient
                .bindToRouterFunction(RouterFunctions.route(DELETE(testingPath + "{username}/follow"), userHandler::unfollow))
                .configureClient().build()
        var result= client.delete().uri(testingPath + profileUserName + "/follow").exchange()

        then: 'the author is updated as unfollowed and updated profile is in the response'
        1 * userSessionProvider.getCurrentUserOrEmpty()  >> Mono.just(currentUser)
        currentUserProfile.setFollowing(false)
        1 * userService.unfollow(profileUserName, currentUser) >> Mono.just(currentUserProfile)
        result.expectStatus().isOk()
        result.expectBody()
                .jsonPath('$.profile.username').isEqualTo(currentUserProfile.getUsername())
                .jsonPath('$.profile.bio').isEqualTo(currentUserProfile.getBio())
                .jsonPath('$.profile.image').isEqualTo(currentUserProfile.getImage())
//                .jsonPath('$.profile.following').isEqualTo(false)
    }
}
