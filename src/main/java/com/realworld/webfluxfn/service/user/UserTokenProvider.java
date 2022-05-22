package com.realworld.webfluxfn.service.user;

public interface UserTokenProvider {
    String getToken(String userId);
}
