package com.easystation.auth.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generateToken(String username, Set<String> roles) {
        return Jwt.issuer("https://easystation.com/issuer")
                .upn(username)
                .groups(roles)
                .expiresIn(86400)
                .sign();
    }
}
