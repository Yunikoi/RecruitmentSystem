package com.recruitment.service;

import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.TalentMatchItem;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalentCrmService {

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final AiMatchingService aiMatchingService;
    private final ComplianceService complianceService;

    public TalentCrmService(ApplicationRepository applicationRepository,
                            PositionRepository positionRepository,
                            AiMatchingService aiMatchingService,
                            ComplianceService complianceService) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.aiMatchingService = aiMatchingService;
        this.complianceService = complianceService;
    }

    public List<TalentMatchItem> matchTalentPool(Long positionId) {
        UserContextHolder.require();
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        return applicationRepository.findByInTalentPoolTrueOrderByMatchScoreDesc().stream()
                .map(app -> {
                    AiMatchingService.MatchResult match = aiMatchingService.analyze(
                            position, app.getResumeText(), app.getParsedSkills());
                    TalentMatchItem item = new TalentMatchItem();
                    item.setApplicationId(app.getId());
                    item.setCandidateName(app.getCandidateName());
                    item.setSkills(app.getParsedSkills());
                    item.setPreviousScore(app.getMatchScore());
                    item.setMatchScore(match.score());
                    item.setHighlights(match.highlights());
                    item.setSilverMedal(app.getStage() == ApplicationStage.REJECTED);
                    return item;
                })
                .filter(i -> i.getMatchScore() >= 75)
                .sorted(Comparator.comparingInt(TalentMatchItem::getMatchScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Transactional
    public TalentMatchItem activateTalent(Long applicationId, Long positionId) {
        var user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可激活人才");
        }
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "人才记录不存在"));
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        app.setInTalentPool(true);
        app.setAiFeedback("【激活邀请】HR 邀请您关注新岗位「" + position.getTitle() + "」，欢迎重新投递或联系 HR。");
        applicationRepository.save(app);
        complianceService.log("TALENT_ACTIVATE", "Application", applicationId,
                "激活人才至岗位 " + positionId);
        TalentMatchItem item = new TalentMatchItem();
        item.setApplicationId(applicationId);
        item.setCandidateName(app.getCandidateName());
        item.setMatchScore(app.getMatchScore());
        item.setHighlights("已发送激活邀请");
        return item;
    }
}
