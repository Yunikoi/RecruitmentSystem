package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PositionMatchDto {
    private Long positionId;
    private String title;
    private String department;
    private String description;
    private Integer matchScore;
    private String highlights;
    private String risks;
    private String recommendation;
    private List<String> matchedSkills;
    private List<String> gapSkills;
}
