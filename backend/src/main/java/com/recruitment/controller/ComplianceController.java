package com.recruitment.controller;

import com.recruitment.dto.ApiResponse;
import com.recruitment.entity.AuditLog;
import com.recruitment.service.ComplianceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webapi/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/settings")
    public ApiResponse<Map<String, Boolean>> getSettings() {
        return ApiResponse.success(complianceService.getSettings());
    }

    @PutMapping("/settings")
    public ApiResponse<Map<String, Boolean>> updateSettings(@RequestBody Map<String, Boolean> settings) {
        return ApiResponse.success(complianceService.updateSettings(settings));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditLog>> auditLogs() {
        return ApiResponse.success(complianceService.listAuditLogs());
    }
}
