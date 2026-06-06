package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.dto.LoginRequest;
import com.recruitment.dto.LoginResponse;
import com.recruitment.entity.User;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenStore tokenStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        UserContext context = toContext(user);
        String token = tokenStore.createToken(context);
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getDisplayName(), user.getDepartment(), user.getRole());
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenStore.remove(token);
        }
    }

    public UserContext getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenStore.get(token);
    }

    public UserContext currentUser() {
        return com.recruitment.context.UserContextHolder.require();
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private UserContext toContext(User user) {
        return new UserContext(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getDepartment(), user.getRole());
    }
}
