package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.entity.AuditLog;
import com.recruitment.entity.SystemSetting;
import com.recruitment.entity.UserRole;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.AuditLogRepository;
import com.recruitment.repository.SystemSettingRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

@Service
public class ComplianceService {

    public static final String KEY_BLIND_HIRING = "blindHiringEnabled";
    public static final String KEY_BLIND_REVIEW = "blindReviewEnabled";

    private final SystemSettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;

    public ComplianceService(SystemSettingRepository settingRepository,
                             AuditLogRepository auditLogRepository) {
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public boolean isBlindHiringEnabled() {
        return "true".equalsIgnoreCase(getSetting(KEY_BLIND_HIRING, "false"));
    }

    public boolean isBlindReviewEnabled() {
        return "true".equalsIgnoreCase(getSetting(KEY_BLIND_REVIEW, "false"));
    }

    public Map<String, Boolean> getSettings() {
        return Map.of(
                "blindHiringEnabled", isBlindHiringEnabled(),
                "blindReviewEnabled", isBlindReviewEnabled()
        );
    }

    @Transactional
    public Map<String, Boolean> updateSettings(Map<String, Boolean> settings) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可修改合规配置");
        }
        if (settings.containsKey("blindHiringEnabled")) {
            saveSetting(KEY_BLIND_HIRING, String.valueOf(settings.get("blindHiringEnabled")));
        }
        if (settings.containsKey("blindReviewEnabled")) {
            saveSetting(KEY_BLIND_REVIEW, String.valueOf(settings.get("blindReviewEnabled")));
        }
        log("UPDATE_COMPLIANCE", "SystemSetting", null, "更新合规配置: " + settings);
        return getSettings();
    }

    public List<AuditLog> listAuditLogs() {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.EXECUTIVE) {
            throw new BusinessException(403, "无权查看审计日志");
        }
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void log(String action, String targetType, Long targetId, String detail) {
        UserContext user = UserContextHolder.get();
        AuditLog log = new AuditLog();
        if (user != null) {
            log.setUserId(user.getUserId());
            log.setUsername(user.getDisplayName());
            log.setRole(user.getRole().name());
        } else {
            log.setUsername("SYSTEM");
        }
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIpAddress(resolveIp());
        auditLogRepository.save(log);
    }

    public String anonymizeName(String name) {
        if (name == null || name.isBlank()) return "候选人#***";
        if (name.length() <= 1) return name.charAt(0) + "**";
        return name.charAt(0) + "**";
    }

    private String getSetting(String key, String defaultValue) {
        return settingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    private void saveSetting(String key, String value) {
        SystemSetting s = settingRepository.findById(key).orElse(new SystemSetting());
        s.setSettingKey(key);
        s.setSettingValue(value);
        settingRepository.save(s);
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                return req.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}
