package com.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentPoolItem {
    private Long applicationId;
    private String name;
    private String skills;
    private Integer matchScore;
}
