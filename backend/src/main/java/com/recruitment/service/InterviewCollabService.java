package com.recruitment.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewCollabService {

    private final Map<Long, String> codeSessions = new ConcurrentHashMap<>();

    public String getCode(Long applicationId) {
        return codeSessions.getOrDefault(applicationId, "// 协同编程区\nfunction solution() {\n  return 'Hello TalentFlow';\n}\n");
    }

    public void saveCode(Long applicationId, String code) {
        codeSessions.put(applicationId, code != null ? code : "");
    }
}
