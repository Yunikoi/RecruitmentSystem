package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MockInterviewResultResponse {
    private Long sessionId;
    private List<String> questions;
    private String tips;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
