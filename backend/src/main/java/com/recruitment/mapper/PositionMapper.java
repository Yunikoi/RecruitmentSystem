package com.recruitment.mapper;

import com.recruitment.dto.PositionResponse;
import com.recruitment.entity.Position;

public final class PositionMapper {

    private PositionMapper() {
    }

    public static PositionResponse toResponse(Position position) {
        PositionResponse response = new PositionResponse();
        response.setId(position.getId());
        response.setTitle(position.getTitle());
        response.setDescription(position.getDescription());
        response.setStatus(position.getStatus());
        response.setCreatedAt(position.getCreatedAt());
        response.setUpdatedAt(position.getUpdatedAt());
        response.setApprover(position.getApprover());
        response.setApprovalComment(position.getApprovalComment());
        response.setPublishedAt(position.getPublishedAt());
        response.setCreatedById(position.getCreatedById());
        response.setCreatedByName(position.getCreatedByName());
        response.setDepartment(position.getDepartment());
        response.setPositionType(position.getPositionType());
        response.setWorkflowSteps(position.getWorkflowSteps());
        response.setSkillTags(position.getSkillTags());
        return response;
    }
}
