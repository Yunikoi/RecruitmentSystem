package com.recruitment.dto;

import lombok.Data;

@Data
public class TalentMatchItem {
    private Long applicationId;
    private String candidateName;
    private String skills;
    private Integer previousScore;
    private Integer matchScore;
    private String highlights;
    private Boolean silverMedal;
}
