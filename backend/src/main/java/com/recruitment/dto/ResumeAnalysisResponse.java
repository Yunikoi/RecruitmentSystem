package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisResponse {
    private ResumeProfileDto profile;
    private ResumeSkillsDto skills;
    private List<ResumeExperienceDto> experiences;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private Integer overallScore;
    private String careerDirection;
    private String analysisSummary;
    private List<PositionMatchDto> matchedPositions;
    private String rawText;
    private Boolean aiPowered;
}
