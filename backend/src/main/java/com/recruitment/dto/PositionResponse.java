package com.recruitment.dto;

import com.recruitment.entity.PositionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PositionResponse {

    private Long id;
    private String title;
    private String description;
    private PositionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approver;
    private String approvalComment;
    private LocalDateTime publishedAt;
    private Long createdById;
    private String createdByName;
    private String department;
    private String positionType;
    private String workflowSteps;
    private String skillTags;
}
