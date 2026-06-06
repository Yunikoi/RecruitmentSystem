package com.recruitment.repository;

import com.recruitment.entity.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {

    List<InterviewSlot> findByPositionIdAndBookedFalseOrderByStartTimeAsc(Long positionId);

    List<InterviewSlot> findByBookedApplicationId(Long applicationId);
}
