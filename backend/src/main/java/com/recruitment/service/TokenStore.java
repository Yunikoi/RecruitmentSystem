package com.recruitment.service;

import com.recruitment.context.UserContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, UserContext> tokens = new ConcurrentHashMap<>();

    public String createToken(UserContext context) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, context);
        return token;
    }

    public UserContext get(String token) {
        return tokens.get(token);
    }

    public void remove(String token) {
        tokens.remove(token);
    }
}
