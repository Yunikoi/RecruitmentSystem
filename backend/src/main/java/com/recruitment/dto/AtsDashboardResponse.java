package com.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtsDashboardResponse {
    private long totalApplications;
    private long activePositions;
    private double avgTimeToHireDays;
    private double offerAcceptRate;
    private Map<String, Long> funnelByStage;
    private Map<String, Long> byChannel;
    private Map<String, Long> byDepartment;
    private List<ChannelRoiItem> channelRoi;
    private List<TalentPoolItem> topTalentPool;
    private List<HirePredictionItem> hirePredictions;
    private List<ChurnMatrixItem> churnMatrix;
    private String biInsight;
}
