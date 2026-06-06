package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class DuplicateGroupResponse {
    private String reason;
    private List<Long> applicationIds;
    private List<String> candidateNames;
    private List<String> emails;
}
