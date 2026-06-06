package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequest {
    private Long applicationId;
    private Long interviewerId;
    private String type;
    private LocalDateTime scheduledAt;
    private String location;
}
