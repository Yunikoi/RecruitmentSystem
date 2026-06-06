package com.recruitment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewResponseRequest {
    /** 接受备注 / 拒绝原因 / 改期说明 */
    private String note;
    /** 改期时期望的时间 */
    private LocalDateTime preferredTime;
}
