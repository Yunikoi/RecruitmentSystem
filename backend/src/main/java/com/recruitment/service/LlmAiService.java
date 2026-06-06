package com.recruitment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmAiService {

    private static final Logger log = LoggerFactory.getLogger(LlmAiService.class);

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public LlmAiService(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiProperties.getTimeoutSeconds() * 1000);
        factory.setReadTimeout(aiProperties.getTimeoutSeconds() * 1000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 调用 LLM，失败或未配置时返回 null。
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!aiProperties.isConfigured()) {
            return null;
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userPrompt));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", aiProperties.getModel());
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("stream", false);

            String url = aiProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions";
            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                log.warn("LLM 返回空内容: {}", response);
                return null;
            }
            return content.asText().trim();
        } catch (Exception e) {
            log.warn("LLM 调用失败: {}", e.getMessage());
            return null;
        }
    }

    /** 从 LLM 回复中提取 JSON 对象文本（兼容 markdown 代码块） */
    public JsonNode parseJsonFromReply(String reply) {
        if (reply == null || reply.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(reply);
        } catch (Exception ignored) {
            // continue
        }
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        if (start >= 0 && end > start) {
            try {
                return objectMapper.readTree(reply.substring(start, end + 1));
            } catch (Exception ignored) {
                // continue
            }
        }
        start = reply.indexOf('[');
        end = reply.lastIndexOf(']');
        if (start >= 0 && end > start) {
            try {
                return objectMapper.readTree(reply.substring(start, end + 1));
            } catch (Exception e) {
                log.warn("解析 LLM JSON 失败");
            }
        }
        return null;
    }
}
