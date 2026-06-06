package com.recruitment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.context.UserContextHolder;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 岗位与候选人阶段流转的唯一入口。
 * 所有 ApplicationStage / PositionStatus 变更必须经过本服务。
 */
@Service
public class WorkflowService {

    public static final Map<String, List<String>> PRESETS = Map.of(
            "TECH", List.of("SCREENING", "WRITTEN_TEST", "AI_INTERVIEW", "BUSINESS_INTERVIEW", "OFFER"),
            "SENIOR", List.of("SCREENING", "BUSINESS_INTERVIEW", "BUSINESS_INTERVIEW", "HR_INTERVIEW", "BACKGROUND_CHECK", "OFFER"),
            "GENERAL", List.of("SCREENING", "AI_INTERVIEW", "BUSINESS_INTERVIEW", "HR_INTERVIEW", "OFFER")
    );

    private static final Map<PositionStatus, Set<PositionStatus>> POSITION_TRANSITIONS = Map.of(
            PositionStatus.DRAFT, Set.of(PositionStatus.PENDING),
            PositionStatus.PENDING, Set.of(PositionStatus.PUBLISHED, PositionStatus.DRAFT),
            PositionStatus.PUBLISHED, Set.of(PositionStatus.CLOSED),
            PositionStatus.CLOSED, Set.of()
    );

    private static final Set<ApplicationStage> TERMINAL_APPLICATION_STAGES =
            Set.of(ApplicationStage.REJECTED, ApplicationStage.HIRED);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PositionRepository positionRepository;

    public WorkflowService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<String> getWorkflow(Position position) {
        if (position.getWorkflowSteps() != null && !position.getWorkflowSteps().isBlank()) {
            try {
                return objectMapper.readValue(position.getWorkflowSteps(), new TypeReference<>() {});
            } catch (Exception ignored) {
            }
        }
        String type = position.getPositionType() != null ? position.getPositionType() : "GENERAL";
        return new ArrayList<>(PRESETS.getOrDefault(type, PRESETS.get("GENERAL")));
    }

    public List<String> getWorkflowByPositionId(Long positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        return getWorkflow(position);
    }

    public List<String> updateWorkflow(Long positionId, List<String> steps) {
        var user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可配置工作流");
        }
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        try {
            position.setWorkflowSteps(objectMapper.writeValueAsString(steps));
        } catch (Exception e) {
            throw new BusinessException(400, "工作流格式无效");
        }
        positionRepository.save(position);
        return steps;
    }

    /** 新建岗位时的初始状态（非流转） */
    public Position initializePositionAsDraft(Position position) {
        position.setStatus(PositionStatus.DRAFT);
        return position;
    }

    /** 岗位状态流转（PositionStatus） */
    public Position transitionPositionStatus(Position position, PositionStatus target) {
        PositionStatus current = position.getStatus();
        if (current == target) {
            return position;
        }
        Set<PositionStatus> allowed = POSITION_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessException(400,
                    "不允许从「" + current + "」流转到「" + target + "」，请通过 WorkflowService 合法路径操作");
        }
        position.setStatus(target);
        if (target == PositionStatus.PUBLISHED && position.getPublishedAt() == null) {
            position.setPublishedAt(LocalDateTime.now());
        }
        return position;
    }

    /** 候选人申请阶段流转（ApplicationStage） */
    public Application transitionApplicationStage(Application app, ApplicationStage target, String trigger) {
        ApplicationStage current = app.getStage();
        if (current == target) {
            return app;
        }
        if (!TERMINAL_APPLICATION_STAGES.contains(target)) {
            Position position = positionRepository.findById(app.getPositionId())
                    .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
            assertStageInWorkflow(position, target);
            assertAiInterviewPrerequisite(app, position, target);
        }
        app.setStage(target);
        app.setStageUpdatedAt(LocalDateTime.now());
        return app;
    }

    public ApplicationStage stageForInterviewType(String interviewType) {
        return switch (interviewType != null ? interviewType.toUpperCase() : "") {
            case "AI" -> ApplicationStage.AI_INTERVIEW;
            case "BUSINESS" -> ApplicationStage.BUSINESS_INTERVIEW;
            case "HR" -> ApplicationStage.HR_INTERVIEW;
            default -> throw new BusinessException(400, "未知面试类型: " + interviewType);
        };
    }

    private void assertStageInWorkflow(Position position, ApplicationStage target) {
        if (target == ApplicationStage.APPLIED) {
            return;
        }
        List<String> workflow = getWorkflow(position);
        String targetStep = target.name();
        if (!workflow.contains(targetStep)
                && target != ApplicationStage.OFFER
                && target != ApplicationStage.HIRED) {
            throw new BusinessException(400,
                    "阶段「" + targetStep + "」不在岗位「" + position.getTitle() + "」的工作流中");
        }
    }

    /**
     * 工作流含 AI 初试时，进入业务面及之后阶段须已完成 AI 初试（有 aiInterviewScore）。
     */
    private void assertAiInterviewPrerequisite(Application app, Position position, ApplicationStage target) {
        List<String> workflow = getWorkflow(position);
        if (!workflow.contains("AI_INTERVIEW")) {
            return;
        }
        if (target == ApplicationStage.BUSINESS_INTERVIEW
                || target == ApplicationStage.HR_INTERVIEW
                || target == ApplicationStage.OFFER
                || target == ApplicationStage.HIRED) {
            if (app.getAiInterviewScore() == null) {
                throw new BusinessException(400, "须先完成 AI 初试，方可安排/进入后续人工面试");
            }
        }
    }
}
