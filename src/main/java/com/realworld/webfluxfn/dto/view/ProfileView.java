package com.realworld.webfluxfn.dto.view;

import com.realworld.webfluxfn.persistence.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProfileView {
    private String username;

    private String bio;

    private String image;

    private boolean following;

    public static ProfileView toUnfollowedProfileView(final User profileUser) {
        return toProfileView(profileUser, false);
    }

    public static ProfileView toFollowedProfileView(final User profileUser) {
        return toProfileView(profileUser, true);
    }

    public static ProfileView toOwnProfile(final User user) {
        return convertToProfileViewByViewerUser(user, user);
    }

    public static ProfileView convertToProfileViewByViewerUser(final User profileUser, final User viewerUser) {
        return toProfileView(profileUser, profileUser.isFollowedBy(viewerUser));
    }

    private static ProfileView toProfileView(final User profileUser, final boolean beingfollowed) {
        return ProfileView.builder()
                .username(profileUser.getUsername())
                .bio(profileUser.getBio())
                .image(profileUser.getImage())
                .following(beingfollowed).build();
    }
}
