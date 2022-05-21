package com.realworld.testharness;

import com.realworld.webfluxfn.security.JwtConfig;
import com.realworld.webfluxfn.security.SecurityConfig;
import com.realworld.webfluxfn.security.JwtSigner;
import com.realworld.webfluxfn.security.TokenExtractor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import({SecurityConfig.class, TokenExtractor.class, JwtSigner.class, JwtConfig.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportAppSecurity {
}
