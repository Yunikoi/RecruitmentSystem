package com.recruitment.repository;

import com.recruitment.entity.MockInterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockInterviewSessionRepository extends JpaRepository<MockInterviewSession, Long> {

    long countByApplicationId(Long applicationId);

    List<MockInterviewSession> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
