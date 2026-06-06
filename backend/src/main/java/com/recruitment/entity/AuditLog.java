package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String role;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 50)
    private String targetType;

    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length = 64)
    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
