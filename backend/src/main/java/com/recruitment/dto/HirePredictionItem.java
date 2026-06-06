package com.recruitment.dto;

import lombok.Data;

@Data
public class HirePredictionItem {
    private String department;
    private long openPositions;
    private int estimatedDays;
    private double estimatedBudgetWan;
    private String suggestion;
}
