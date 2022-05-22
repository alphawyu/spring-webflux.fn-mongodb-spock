package com.realworld.webfluxfn.dto.view;

import com.realworld.webfluxfn.persistence.entity.User;
import com.realworld.webfluxfn.service.user.UserSessionProvider;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

//@Data
@Accessors(chain = true)
//@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserView {
    private String email;

    private String token;

    private String username;

    private String bio;

    private String image;

    public static UserView fromUserAndToken(final UserSessionProvider.UserSession userSession) {
        return fromUserAndToken(userSession.user(), userSession.token());
    }

    public static UserView fromUserAndToken(final User user, final String token) {
        return UserView.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .image(user.getImage())
                .token(token).build();
    }

}
