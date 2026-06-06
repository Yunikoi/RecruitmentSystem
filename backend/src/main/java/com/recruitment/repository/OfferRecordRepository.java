package com.recruitment.repository;

import com.recruitment.entity.OfferRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferRecordRepository extends JpaRepository<OfferRecord, Long> {

    Optional<OfferRecord> findByApplicationId(Long applicationId);
}
