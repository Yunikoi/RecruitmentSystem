package com.recruitment.dto;

import com.recruitment.entity.ApplicationStage;
import lombok.Data;

@Data
public class StageUpdateRequest {
    private ApplicationStage stage;
    private String feedback;
}
