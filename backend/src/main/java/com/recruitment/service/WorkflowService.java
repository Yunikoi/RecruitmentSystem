package com.recruitment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.context.UserContextHolder;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorkflowService {

    public static final Map<String, List<String>> PRESETS = Map.of(
            "TECH", List.of("SCREENING", "WRITTEN_TEST", "AI_INTERVIEW", "BUSINESS_INTERVIEW", "OFFER"),
            "SENIOR", List.of("SCREENING", "BUSINESS_INTERVIEW", "BUSINESS_INTERVIEW", "HR_INTERVIEW", "BACKGROUND_CHECK", "OFFER"),
            "GENERAL", List.of("SCREENING", "AI_INTERVIEW", "BUSINESS_INTERVIEW", "HR_INTERVIEW", "OFFER")
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Transactional
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

    public List<String> getWorkflowByPositionId(Long positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        return getWorkflow(position);
    }

    private final PositionRepository positionRepository;

    public WorkflowService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }
}
