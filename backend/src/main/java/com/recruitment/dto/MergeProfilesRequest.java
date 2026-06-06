package com.recruitment.dto;

import lombok.Data;

import java.util.List;

@Data
public class MergeProfilesRequest {
    private Long primaryId;
    private List<Long> mergeIds;
}
