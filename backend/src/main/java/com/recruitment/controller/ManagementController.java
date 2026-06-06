package com.recruitment.controller;

import com.recruitment.dto.ApiResponse;
import com.recruitment.dto.AtsDashboardResponse;
import com.recruitment.service.AtsAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webapi/management")
public class ManagementController {

    private final AtsAnalyticsService analyticsService;

    public ManagementController(AtsAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AtsDashboardResponse> dashboard() {
        return ApiResponse.success(analyticsService.getDashboard());
    }
}
