package com.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {

    private int total;
    private int successCount;
    private int failCount;
    private List<String> errors;
    private List<PositionResponse> imported;
}
