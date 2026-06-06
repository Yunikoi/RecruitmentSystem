package com.recruitment.controller;

import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.ApiResponse;
import com.recruitment.dto.LoginRequest;
import com.recruitment.dto.LoginResponse;
import com.recruitment.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webapi/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(extractToken(request));
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        var user = UserContextHolder.require();
        return ApiResponse.success(new LoginResponse(null, user.getUserId(), user.getUsername(),
                user.getDisplayName(), user.getDepartment(), user.getRole()));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
