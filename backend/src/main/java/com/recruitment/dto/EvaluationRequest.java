package com.recruitment.dto;

import lombok.Data;

@Data
public class EvaluationRequest {
    private Long interviewId;
    private Long applicationId;
    private Integer technicalScore;
    private Integer communicationScore;
    private Integer cultureScore;
    private String strengths;
    private String weaknesses;
    private String recommendation;
    private String result;
}
