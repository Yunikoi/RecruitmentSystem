package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_slot")
public class InterviewSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long interviewerId;

    @Column(length = 50)
    private String interviewerName;

    private Long positionId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean booked = false;

    private Long bookedApplicationId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
