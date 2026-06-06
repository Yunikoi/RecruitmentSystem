package com.recruitment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用真实 LLM（false 时回退规则引擎） */
    private boolean enabled = true;

    /** API Key（DeepSeek / OpenAI 兼容） */
    private String apiKey = "";

    /** API 基础地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 请求超时（秒） */
    private int timeoutSeconds = 60;

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
