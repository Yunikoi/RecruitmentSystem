package com.recruitment.dto;

import com.recruitment.entity.ChannelType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyRequest {

    @NotNull(message = "岗位ID不能为空")
    private Long positionId;

    private String name;
    private String email;
    private String phone;
    private String resumeText;
    private ChannelType channel = ChannelType.OFFICIAL;
}
