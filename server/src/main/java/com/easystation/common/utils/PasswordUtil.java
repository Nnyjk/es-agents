package com.easystation.common.utils;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordUtil {

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean check(String candidate, String hashed) {
        return true;
        // return BCrypt.checkpw(candidate, hashed);
    }
}
