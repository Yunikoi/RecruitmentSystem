package com.recruitment.dto;

import lombok.Data;

@Data
public class AiInterviewReplyResponse {
    private String aiMessage;
    private int questionIndex;
    private int totalQuestions;
    private boolean finished;
    private Integer score;
    private String summary;
}
