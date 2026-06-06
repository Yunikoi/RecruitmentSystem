package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeSkillsDto {
    private List<String> core;
    private List<String> tools;
    private List<String> soft;
}
