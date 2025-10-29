package com.realworld.webfluxfn.security;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    int sessionTime = 86400;
}
