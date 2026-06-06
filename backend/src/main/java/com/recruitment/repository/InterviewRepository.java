package com.recruitment.repository;

import com.recruitment.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationId(Long applicationId);

    List<Interview> findByApplicationIdInOrderByScheduledAtDesc(Collection<Long> applicationIds);

    List<Interview> findByInterviewerIdOrderByScheduledAtDesc(Long interviewerId);

    List<Interview> findAllByOrderByCreatedAtDesc();
}
