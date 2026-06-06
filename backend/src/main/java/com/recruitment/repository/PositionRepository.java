package com.recruitment.repository;

import com.recruitment.entity.Position;
import com.recruitment.entity.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByTitleContainingIgnoreCase(String title);

    List<Position> findByStatus(PositionStatus status);

    List<Position> findByTitleContainingIgnoreCaseAndStatus(String title, PositionStatus status);

    long countByStatus(PositionStatus status);

    List<Position> findByCreatedById(Long createdById);

    List<Position> findByCreatedByIdAndTitleContainingIgnoreCase(Long createdById, String title);

    List<Position> findByCreatedByIdAndStatus(Long createdById, PositionStatus status);

    List<Position> findByCreatedByIdAndTitleContainingIgnoreCaseAndStatus(
            Long createdById, String title, PositionStatus status);
}
