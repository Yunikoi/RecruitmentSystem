package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MockInterviewStatusResponse {
    private Long applicationId;
    private int usedCount;
    private int maxCount;
    private boolean canStart;
    private List<MockInterviewResultResponse> history;
}
