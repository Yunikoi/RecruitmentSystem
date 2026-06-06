package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositionStatus status = PositionStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String approver;

    @Column(columnDefinition = "TEXT")
    private String approvalComment;

    private LocalDateTime publishedAt;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "created_by_name", length = 50)
    private String createdByName;

    @Column(length = 100)
    private String department;

    @Column(length = 30)
    private String positionType = "GENERAL";

    @Column(columnDefinition = "TEXT")
    private String workflowSteps;

    @Column(length = 500)
    private String skillTags;
}
