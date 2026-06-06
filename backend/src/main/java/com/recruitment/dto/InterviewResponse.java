package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private String type;
    private String typeLabel;
    private LocalDateTime scheduledAt;
    private String location;
    private String interviewerName;
    private String status;
    private String statusLabel;
    private Boolean canRespond;
    private String responseNote;
    private LocalDateTime respondedAt;
    private LocalDateTime invitedAt;
    private LocalDateTime expireAt;
    private String invitedByName;
    private String positionTitle;
    private String candidateName;
}
