package com.recruitment.repository;

import com.recruitment.entity.InterviewEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, Long> {

    List<InterviewEvaluation> findByApplicationId(Long applicationId);

    List<InterviewEvaluation> findByInterviewId(Long interviewId);
}
