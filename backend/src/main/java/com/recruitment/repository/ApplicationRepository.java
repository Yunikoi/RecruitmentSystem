package com.recruitment.repository;

import com.recruitment.entity.Application;
import com.recruitment.entity.ApplicationStage;
import com.recruitment.entity.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    List<Application> findByPositionIdOrderByMatchScoreDesc(Long positionId);

    List<Application> findByStage(ApplicationStage stage);

    List<Application> findByPositionIdAndStage(Long positionId, ApplicationStage stage);

    long countByStage(ApplicationStage stage);

    long countByChannel(ChannelType channel);

    long countByInTalentPoolTrue();

    Optional<Application> findByPositionIdAndCandidateId(Long positionId, Long candidateId);

    List<Application> findByCandidateEmail(String email);

    List<Application> findByInTalentPoolTrueOrderByMatchScoreDesc();
}
