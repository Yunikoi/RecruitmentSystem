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
    private final ComplianceService complianceService;

    public TalentCrmService(ApplicationRepository applicationRepository,
                            PositionRepository positionRepository,
                            ComplianceService complianceService) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.complianceService = complianceService;
    }

    /**
     * 人才库匹配：仅使用已持久化的 matchScore，禁止查询时实时调用 LLM 计算。
     */
    public List<TalentMatchItem> matchTalentPool(Long positionId) {
        UserContextHolder.require();
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        return applicationRepository.findByInTalentPoolTrueOrderByMatchScoreDesc().stream()
                .map(app -> toTalentMatchItem(app, position))
                .filter(i -> i.getMatchScore() != null && i.getMatchScore() >= 75)
                .sorted(Comparator.comparingInt(TalentMatchItem::getMatchScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private TalentMatchItem toTalentMatchItem(Application app, Position targetPosition) {
        int crossScore = estimateCrossPositionScore(app, targetPosition);
        TalentMatchItem item = new TalentMatchItem();
        item.setApplicationId(app.getId());
        item.setCandidateName(app.getCandidateName());
        item.setSkills(app.getParsedSkills());
        item.setPreviousScore(app.getMatchScore());
        item.setMatchScore(crossScore);
        item.setHighlights(app.getMatchHighlights() != null ? app.getMatchHighlights()
                : "基于历史投递匹配分与技能标签估算");
        item.setSilverMedal(app.getStage() == ApplicationStage.REJECTED);
        return item;
    }

    /** 规则估算跨岗匹配，不触发 LLM */
    private int estimateCrossPositionScore(Application app, Position position) {
        if (app.getMatchScore() == null) {
            return 0;
        }
        int base = app.getMatchScore();
        String skills = (app.getParsedSkills() != null ? app.getParsedSkills() : "").toLowerCase();
        String tags = (position.getSkillTags() != null ? position.getSkillTags() : "").toLowerCase();
        if (tags.isBlank()) {
            return base;
        }
        long hit = Arrays.stream(tags.split("[,，、/\\s]+"))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(skills::contains)
                .count();
        long total = Arrays.stream(tags.split("[,，、/\\s]+")).filter(s -> !s.isBlank()).count();
        int bonus = total > 0 ? (int) (hit * 15 / total) : 0;
        return Math.min(99, base / 2 + bonus);
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
