package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long interviewerId;

    @Column(length = 50)
    private String interviewerName;

    @Column(nullable = false, length = 30)
    private String type;

    private LocalDateTime scheduledAt;

    @Column(length = 200)
    private String location;

    /** PENDING / ACCEPTED / DECLINED / RESCHEDULE_REQUESTED / CANCELLED / COMPLETED */
    @Column(length = 30)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String questions;

    @Column(length = 500)
    private String responseNote;

    private LocalDateTime respondedAt;

    private LocalDateTime expireAt;

    private Long invitedById;

    @Column(length = 50)
    private String invitedByName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
