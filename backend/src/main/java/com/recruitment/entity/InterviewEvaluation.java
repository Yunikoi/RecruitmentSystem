package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_evaluation")
public class InterviewEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long interviewId;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long evaluatorId;

    @Column(length = 50)
    private String evaluatorName;

    private Integer technicalScore;
    private Integer communicationScore;
    private Integer cultureScore;
    private Integer overallScore;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(length = 20)
    private String result;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
