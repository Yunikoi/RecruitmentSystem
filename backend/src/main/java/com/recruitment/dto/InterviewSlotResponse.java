package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewSlotResponse {
    private Long id;
    private String interviewerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean booked;
}
