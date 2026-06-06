package com.recruitment.dto;

import lombok.Data;

@Data
public class ShareCandidateRequest {
    private Long applicationId;
    private String targetUser;
}
