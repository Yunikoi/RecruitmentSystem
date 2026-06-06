package com.recruitment.dto;

import lombok.Data;

@Data
public class ResumeProfileDto {
    private String name;
    private String email;
    private String phone;
    private Integer yearsOfExperience;
    private String education;
    private String currentTitle;
    private String summary;
}
