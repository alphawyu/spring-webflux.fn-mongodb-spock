package com.realworld.webfluxfn.service.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {
    private final transient PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encodePassword(final String rowPassword) {
        return encoder.encode(rowPassword);
    }

    public boolean matchesRowPasswordWithEncodedPassword(final String rowPassword, final String encodedPassword) {
        return encoder.matches(rowPassword, encodedPassword);
    }
}
