package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowUpdateRequest {
    private List<String> steps;
}
