package com.recruitment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_setting")
public class SystemSetting {

    @Id
    @Column(length = 80)
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
