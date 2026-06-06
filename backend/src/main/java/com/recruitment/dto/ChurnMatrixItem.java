package com.recruitment.dto;

import lombok.Data;

@Data
public class ChurnMatrixItem {
    private String channel;
    private double retentionScore;
    private long hires;
    private String recommendation;
}
