package com.recruitment.dto;

import lombok.Data;

@Data
public class BackgroundCheckResponse {
    private Long applicationId;
    private String status;
    private String reportSummary;
    private String provider;
}
