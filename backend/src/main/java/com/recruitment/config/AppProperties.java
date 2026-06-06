package com.recruitment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir = "uploads/resumes";

    /** 本地开发可开启；生产环境默认关闭 */
    private boolean seedDemoData = false;

    private List<String> corsAllowedOriginPatterns = new ArrayList<>(List.of("*"));

    /** 支持环境变量逗号分隔，例如 http://localhost,https://*.vercel.app */
    public void setCorsAllowedOrigins(String csv) {
        if (csv != null && !csv.isBlank()) {
            this.corsAllowedOriginPatterns = Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}
