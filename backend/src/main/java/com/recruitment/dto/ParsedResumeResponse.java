package com.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResumeResponse {
    private String name;
    private String email;
    private String phone;
    private String skills;
    private String summary;
    private String rawText;
}
