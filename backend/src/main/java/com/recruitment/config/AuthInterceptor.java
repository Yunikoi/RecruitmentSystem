package com.recruitment.config;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.entity.UserRole;
import com.recruitment.exception.BusinessException;
import com.recruitment.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_PATHS = Set.of("/webapi/auth/login", "/webapi/health");

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if (PUBLIC_PATHS.contains(path) && ("POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method))) return true;
        if (isVisitorPath(path, method)) return true;

        String token = extractToken(request);
        UserContext user = authService.getUserByToken(token);
        if (user == null) throw new BusinessException(401, "请先登录");
        UserContextHolder.set(user);

        if (path.startsWith("/webapi/compliance/")) {
            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.EXECUTIVE) {
                throw new BusinessException(403, "仅管理层/HR可访问合规模块");
            }
        } else if (path.startsWith("/webapi/candidate/")) {
            if (user.getRole() != UserRole.CANDIDATE) {
                throw new BusinessException(403, "仅求职者账号可访问");
            }
        } else if (path.startsWith("/webapi/recruiter/")) {
            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.INTERVIEWER) {
                throw new BusinessException(403, "仅HR/面试官可访问");
            }
        } else if (path.startsWith("/webapi/management/")) {
            if (user.getRole() != UserRole.EXECUTIVE && user.getRole() != UserRole.ADMIN) {
                throw new BusinessException(403, "仅管理层可访问");
            }
        } else if (isAdminOnly(path, method) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅招聘管理员可执行此操作");
        } else if (isDepartmentOnly(path, method) && user.getRole() != UserRole.DEPARTMENT) {
            throw new BusinessException(403, "仅部门账号可执行此操作");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private boolean isVisitorPath(String path, String method) {
        if (!"GET".equalsIgnoreCase(method)) return false;
        if ("/webapi/positions".equals(path)) return true;
        return path.startsWith("/webapi/positions/published")
                || path.matches("/webapi/public/positions/\\d+");
    }

    private boolean isAdminOnly(String path, String method) {
        if ("GET".equalsIgnoreCase(method) && "/webapi/positions/statistics".equals(path)) return true;
        if ("POST".equalsIgnoreCase(method)) {
            return path.matches("/webapi/positions/\\d+/approve")
                    || path.matches("/webapi/positions/\\d+/reject")
                    || path.matches("/webapi/positions/\\d+/close");
        }
        return false;
    }

    private boolean isDepartmentOnly(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && "/webapi/positions".equals(path)) return true;
        if ("POST".equalsIgnoreCase(method) && "/webapi/positions/import".equals(path)) return true;
        if ("GET".equalsIgnoreCase(method) && "/webapi/positions/template".equals(path)) return true;
        if ("PUT".equalsIgnoreCase(method) && path.matches("/webapi/positions/\\d+")) return true;
        if ("DELETE".equalsIgnoreCase(method) && path.matches("/webapi/positions/\\d+")) return true;
        if ("POST".equalsIgnoreCase(method) && path.matches("/webapi/positions/\\d+/submit")) return true;
        if ("POST".equalsIgnoreCase(method) && path.matches("/webapi/positions/\\d+/remind")) return true;
        if ("POST".equalsIgnoreCase(method) && path.matches("/webapi/positions/\\d+/share")) return true;
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }
}
