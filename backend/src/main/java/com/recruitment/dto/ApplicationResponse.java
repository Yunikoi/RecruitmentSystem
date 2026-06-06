package com.recruitment.dto;

import com.recruitment.entity.ApplicationStage;
import com.recruitment.entity.ChannelType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationResponse {
    private Long id;
    private Long positionId;
    private String positionTitle;
    private String department;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private ApplicationStage stage;
    private String stageLabel;
    private ChannelType channel;
    private String channelLabel;
    private Integer matchScore;
    private String matchHighlights;
    private String matchRisks;
    private String resumeText;
    private String parsedSkills;
    private String resumeFileName;
    private String resumeContentType;
    private Boolean hasResumeFile;
    private String aiFeedback;
    private Integer aiInterviewScore;
    private String aiInterviewFeedback;
    private Boolean aiInterviewPass;
    private LocalDateTime aiInterviewAt;
    private String recommendedPositions;
    private Boolean inTalentPool;
    private LocalDateTime appliedAt;
    private LocalDateTime stageUpdatedAt;
    private List<InterviewResponse> interviews;
}
