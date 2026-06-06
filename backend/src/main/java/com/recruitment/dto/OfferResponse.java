package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OfferResponse {
    private Long applicationId;
    private String status;
    private LocalDateTime signedAt;
    private String signUrl;
}
