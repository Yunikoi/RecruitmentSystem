package com.recruitment.service;

import com.recruitment.entity.Application;
import com.recruitment.entity.ApplicationStage;
import com.recruitment.entity.Position;
import com.recruitment.entity.PositionStatus;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.PositionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchScoreAsyncService {

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final AiMatchingService aiMatchingService;
    private final WorkflowService workflowService;

    public MatchScoreAsyncService(ApplicationRepository applicationRepository,
                                  PositionRepository positionRepository,
                                  AiMatchingService aiMatchingService,
                                  WorkflowService workflowService) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.aiMatchingService = aiMatchingService;
        this.workflowService = workflowService;
    }

    /** 投递完成后异步触发，禁止在查询接口中实时计算 matchScore。 */
    @Async("matchScoreExecutor")
    public void calculateAfterApply(Long applicationId) {
        calculateAfterApplyInternal(applicationId);
    }

    @Transactional
    public void calculateAfterApplyInternal(Long applicationId) {
        Application app = applicationRepository.findById(applicationId).orElse(null);
        if (app == null || app.getMatchScore() != null) {
            return;
        }
        Position position = positionRepository.findById(app.getPositionId()).orElse(null);
        if (position == null) {
            return;
        }
        AiMatchingService.MatchResult match = aiMatchingService.analyze(
                position, app.getResumeText(), app.getParsedSkills());
        app.setMatchScore(match.score());
        app.setMatchHighlights(match.highlights());
        app.setMatchRisks(match.risks());

        if (match.score() >= 75 && app.getStage() == ApplicationStage.APPLIED) {
            workflowService.transitionApplicationStage(app, ApplicationStage.SCREENING, "AUTO_MATCH");
        }
        applicationRepository.save(app);
    }
}
