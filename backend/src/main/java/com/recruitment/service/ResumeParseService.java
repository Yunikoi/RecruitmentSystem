package com.recruitment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.dto.ParsedResumeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeParseService {

    private static final Pattern EMAIL = Pattern.compile("[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    private static final List<String> SKILL_KEYWORDS = List.of(
            "Java", "Spring", "Spring Boot", "Vue", "React", "Python", "Go", "Kubernetes",
            "Docker", "MySQL", "Redis", "Kafka", "微服务", "分布式", "算法", "数据结构",
            "TypeScript", "Node.js", "AWS", "Git", "Linux", "HTML", "CSS", "Element Plus"
    );

    private final LlmAiService llmAiService;

    public ResumeParseService(LlmAiService llmAiService) {
        this.llmAiService = llmAiService;
    }

    public ParsedResumeResponse parseFile(MultipartFile file) throws Exception {
        return parseBytes(file.getBytes(), file.getOriginalFilename());
    }

    public ParsedResumeResponse parseBytes(byte[] data, String filename) throws Exception {
        String text = extractTextFromBytes(data, filename);
        return parseText(text);
    }

    private String extractTextFromBytes(byte[] data, String filename) throws Exception {
        String name = filename != null ? filename.toLowerCase() : "";
        if (name.endsWith(".pdf")) {
            try (var doc = org.apache.pdfbox.Loader.loadPDF(data)) {
                return new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            }
        }
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            try (var workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(data))) {
                var sheet = workbook.getSheetAt(0);
                var formatter = new org.apache.poi.ss.usermodel.DataFormatter();
                StringBuilder sb = new StringBuilder();
                for (var row : sheet) {
                    for (var cell : row) {
                        sb.append(formatter.formatCellValue(cell)).append(" ");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            }
        }
        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }

    public ParsedResumeResponse parseText(String rawText) {
        String text = rawText != null ? rawText : "";
        ParsedResumeResponse aiParsed = parseWithLlm(text);
        if (aiParsed != null) {
            return aiParsed;
        }
        return parseWithRules(text);
    }

    private ParsedResumeResponse parseWithLlm(String text) {
        if (text.isBlank()) {
            return null;
        }
        String system = "你是简历解析助手，从简历文本提取结构化信息。"
                + "严格只输出 JSON：{\"name\":\"姓名\",\"email\":\"邮箱\",\"phone\":\"手机\",\"skills\":\"技能，顿号分隔\",\"summary\":\"100字以内摘要\"}";
        String user = "请解析以下简历：\n" + truncate(text, 8000);
        String reply = llmAiService.chat(system, user);
        JsonNode json = llmAiService.parseJsonFromReply(reply);
        if (json == null) {
            return null;
        }
        String name = json.path("name").asText("").trim();
        String email = json.path("email").asText("").trim();
        String phone = json.path("phone").asText("").trim();
        String skills = json.path("skills").asText("").trim();
        String summary = json.path("summary").asText("").trim();
        if (name.isEmpty() && email.isEmpty() && skills.isEmpty()) {
            return null;
        }
        if (summary.isEmpty()) {
            summary = text.length() > 200 ? text.substring(0, 200) + "..." : text;
        }
        return new ParsedResumeResponse(
                name.isEmpty() ? "求职者" : name,
                email,
                phone,
                skills,
                summary,
                text
        );
    }

    private ParsedResumeResponse parseWithRules(String text) {
        String email = findFirst(EMAIL, text);
        String phone = findFirst(PHONE, text);
        String name = guessName(text);
        List<String> foundSkills = SKILL_KEYWORDS.stream()
                .filter(s -> text.toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toList());
        String skills = String.join("、", foundSkills);
        String summary = text.length() > 200 ? text.substring(0, 200) + "..." : text;
        return new ParsedResumeResponse(name, email, phone, skills, summary, text);
    }

    private String findFirst(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group() : "";
    }

    private String guessName(String text) {
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.length() >= 2 && line.length() <= 10
                    && !line.contains("@") && !line.matches(".*\\d{5,}.*")) {
                return line;
            }
        }
        return "求职者";
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
