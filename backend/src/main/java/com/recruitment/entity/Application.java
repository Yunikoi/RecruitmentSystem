package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long positionId;

    @Column(nullable = false)
    private Long candidateId;

    @Column(length = 50)
    private String candidateName;

    @Column(length = 100)
    private String candidateEmail;

    @Column(length = 20)
    private String candidatePhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStage stage = ApplicationStage.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType channel = ChannelType.OFFICIAL;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    @Column(length = 255)
    private String resumeFileName;

    @Column(length = 255)
    private String resumeStoredName;

    @Column(length = 100)
    private String resumeContentType;

    @Column(columnDefinition = "TEXT")
    private String parsedSkills;

    private Integer matchScore;

    @Column(columnDefinition = "TEXT")
    private String matchHighlights;

    @Column(columnDefinition = "TEXT")
    private String matchRisks;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(columnDefinition = "TEXT")
    private String recommendedPositions;

    private Boolean inTalentPool = false;

    @CreationTimestamp
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime stageUpdatedAt;

    private Long mergedIntoId;
}
