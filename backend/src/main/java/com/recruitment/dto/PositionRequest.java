package com.recruitment.dto;

import com.recruitment.entity.PositionStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionRequest {

    @NotBlank(message = "岗位名称不能为空")
    private String title;

    @NotBlank(message = "岗位描述不能为空")
    private String description;

    private PositionStatus status;
    private String positionType;
    private String skillTags;
}
