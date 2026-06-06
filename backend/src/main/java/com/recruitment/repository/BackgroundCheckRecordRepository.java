package com.recruitment.repository;

import com.recruitment.entity.BackgroundCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackgroundCheckRecordRepository extends JpaRepository<BackgroundCheckRecord, Long> {

    Optional<BackgroundCheckRecord> findByApplicationId(Long applicationId);
}
