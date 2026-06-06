package com.recruitment.dto;

import lombok.Data;

@Data
public class AiInterviewStartResponse {
    private String sessionId;
    private String aiMessage;
    private int questionIndex;
    private int totalQuestions;
    private String tips;
    private boolean voiceEnabled = true;
}
