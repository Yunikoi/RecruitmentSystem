package com.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRoiItem {
    private String channel;
    private long applications;
    private long hires;
    private double hireRate;
}
