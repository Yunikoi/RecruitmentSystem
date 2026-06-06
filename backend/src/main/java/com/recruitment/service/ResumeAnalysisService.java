package com.recruitment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.dto.*;
import com.recruitment.entity.Position;
import com.recruitment.entity.PositionStatus;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeAnalysisService {

    private static final List<String> CORE_SKILL_KEYWORDS = List.of(
            "Java", "Spring", "Spring Boot", "Vue", "React", "Python", "Go", "Kubernetes",
            "微服务", "分布式", "算法", "TypeScript", "Node.js", "MySQL", "Redis"
    );
    private static final List<String> TOOL_KEYWORDS = List.of(
            "Docker", "Git", "Linux", "Kafka", "AWS", "Jenkins", "Nginx", "Maven", "Gradle"
    );
    private static final List<String> SOFT_KEYWORDS = List.of(
            "沟通", "协作", "团队", "领导力", "抗压", "学习", "创新", "责任心"
    );
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})\\s*[-–~至]\\s*(\\d{4}|至今|present)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EDU_PATTERN = Pattern.compile("(博士|硕士|研究生|本科|学士|大专|专科|PhD|Master|Bachelor)");

    private final ResumeParseService resumeParseService;
    private final AiMatchingService aiMatchingService;
    private final LlmAiService llmAiService;
    private final PositionRepository positionRepository;

    public ResumeAnalysisService(ResumeParseService resumeParseService,
                                 AiMatchingService aiMatchingService,
                                 LlmAiService llmAiService,
                                 PositionRepository positionRepository) {
        this.resumeParseService = resumeParseService;
        this.aiMatchingService = aiMatchingService;
        this.llmAiService = llmAiService;
        this.positionRepository = positionRepository;
    }

    public ResumeAnalysisResponse analyzeFile(MultipartFile file) throws Exception {
        ParsedResumeResponse parsed = resumeParseService.parseFile(file);
        return buildAnalysis(parsed);
    }

    public ResumeAnalysisResponse analyzeText(String text) {
        ParsedResumeResponse parsed = resumeParseService.parseText(text != null ? text : "");
        return buildAnalysis(parsed);
    }

    private ResumeAnalysisResponse buildAnalysis(ParsedResumeResponse parsed) {
        String resumeText = parsed.getRawText() != null ? parsed.getRawText() : "";
        DeepAnalysis deep = analyzeDeepWithLlm(resumeText, parsed);
        boolean aiPowered = deep.aiPowered;

        if (deep.profile == null) {
            deep = analyzeDeepWithRules(resumeText, parsed);
        }

        List<Position> published = positionRepository.findByStatus(PositionStatus.PUBLISHED);
        List<PositionMatchDto> matches = matchPositions(published, resumeText, parsed.getSkills(), aiPowered);

        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setProfile(deep.profile);
        response.setSkills(deep.skills);
        response.setExperiences(deep.experiences);
        response.setStrengths(deep.strengths);
        response.setWeaknesses(deep.weaknesses);
        response.setSuggestions(deep.suggestions);
        response.setOverallScore(deep.overallScore);
        response.setCareerDirection(deep.careerDirection);
        response.setAnalysisSummary(deep.analysisSummary);
        response.setMatchedPositions(matches);
        response.setRawText(resumeText);
        response.setAiPowered(aiPowered);
        return response;
    }

    private List<PositionMatchDto> matchPositions(List<Position> positions, String resumeText,
                                                   String parsedSkills, boolean useLlmForTop) {
        String skills = parsedSkills != null ? parsedSkills : "";
        List<PositionMatchDto> items = new ArrayList<>();

        for (Position position : positions) {
            AiMatchingService.MatchResult match = aiMatchingService.analyzeRulesOnly(position, resumeText, skills);

            PositionMatchDto dto = new PositionMatchDto();
            dto.setPositionId(position.getId());
            dto.setTitle(position.getTitle());
            dto.setDepartment(position.getDepartment());
            dto.setDescription(truncate(position.getDescription(), 200));
            dto.setMatchScore(match.score());
            dto.setHighlights(match.highlights());
            dto.setRisks(match.risks());
            dto.setRecommendation(recommendLevel(match.score()));
            SkillGap gap = detectSkillGap(position, resumeText, skills);
            dto.setMatchedSkills(gap.matched);
            dto.setGapSkills(gap.gaps);
            items.add(dto);
        }

        items.sort((a, b) -> Integer.compare(
                b.getMatchScore() != null ? b.getMatchScore() : 0,
                a.getMatchScore() != null ? a.getMatchScore() : 0));

        if (useLlmForTop && !items.isEmpty()) {
            refineTopMatchesWithLlm(items, resumeText, positions);
        }

        return items;
    }

    private void refineTopMatchesWithLlm(List<PositionMatchDto> items, String resumeText, List<Position> positions) {
        int limit = Math.min(3, items.size());
        Map<Long, Position> positionMap = positions.stream()
                .collect(Collectors.toMap(Position::getId, p -> p, (a, b) -> a));

        for (int i = 0; i < limit; i++) {
            PositionMatchDto item = items.get(i);
            Position position = positionMap.get(item.getPositionId());
            if (position == null) {
                continue;
            }
            AiMatchingService.MatchResult refined = aiMatchingService.analyze(position, resumeText, "");
            item.setMatchScore(refined.score());
            item.setHighlights(refined.highlights());
            item.setRisks(refined.risks());
            item.setRecommendation(recommendLevel(refined.score()));
        }
        items.sort((a, b) -> Integer.compare(
                b.getMatchScore() != null ? b.getMatchScore() : 0,
                a.getMatchScore() != null ? a.getMatchScore() : 0));
    }

    private DeepAnalysis analyzeDeepWithLlm(String resumeText, ParsedResumeResponse parsed) {
        if (resumeText.isBlank()) {
            return new DeepAnalysis(false);
        }

        String system = """
                你是资深简历分析专家与职业规划顾问。请深度解析简历并严格只输出 JSON，不要 markdown：
                {
                  "name":"姓名","email":"邮箱","phone":"手机",
                  "yearsOfExperience":数字或null,
                  "education":"最高学历描述",
                  "currentTitle":"当前/最近职位",
                  "summary":"80字职业摘要",
                  "coreSkills":["核心技能"],
                  "toolSkills":["工具技能"],
                  "softSkills":["软技能"],
                  "experiences":[{"company":"公司","role":"职位","duration":"时间段","highlights":"亮点"}],
                  "strengths":["优势1","优势2","优势3"],
                  "weaknesses":["不足1","不足2"],
                  "suggestions":["改进建议1","改进建议2","改进建议3"],
                  "overallScore":0-100整数,
                  "careerDirection":"适合发展方向",
                  "analysisSummary":"150字综合评价"
                }
                """;

        String user = "请分析以下简历：\n" + truncate(resumeText, 8000);
        String reply = llmAiService.chat(system, user);
        JsonNode json = llmAiService.parseJsonFromReply(reply);
        if (json == null) {
            return new DeepAnalysis(false);
        }

        DeepAnalysis deep = new DeepAnalysis(true);
        deep.profile = buildProfileFromJson(json, parsed);
        deep.skills = buildSkillsFromJson(json, parsed);
        deep.experiences = buildExperiencesFromJson(json);
        deep.strengths = readStringList(json, "strengths");
        deep.weaknesses = readStringList(json, "weaknesses");
        deep.suggestions = readStringList(json, "suggestions");
        deep.overallScore = clamp(json.path("overallScore").asInt(computeCompletenessScore(parsed)), 0, 100);
        deep.careerDirection = json.path("careerDirection").asText(inferCareerDirection(parsed.getSkills()));
        deep.analysisSummary = json.path("analysisSummary").asText(parsed.getSummary());

        if (deep.strengths.isEmpty()) {
            deep.strengths = defaultStrengths(parsed);
        }
        if (deep.suggestions.isEmpty()) {
            deep.suggestions = defaultSuggestions(parsed);
        }
        return deep;
    }

    private DeepAnalysis analyzeDeepWithRules(String resumeText, ParsedResumeResponse parsed) {
        DeepAnalysis deep = new DeepAnalysis(false);

        ResumeProfileDto profile = new ResumeProfileDto();
        profile.setName(parsed.getName());
        profile.setEmail(parsed.getEmail());
        profile.setPhone(parsed.getPhone());
        profile.setYearsOfExperience(estimateYears(resumeText));
        profile.setEducation(findEducation(resumeText));
        profile.setCurrentTitle(guessCurrentTitle(resumeText));
        profile.setSummary(parsed.getSummary());
        deep.profile = profile;

        ResumeSkillsDto skills = new ResumeSkillsDto();
        skills.setCore(matchKeywords(resumeText + " " + parsed.getSkills(), CORE_SKILL_KEYWORDS));
        skills.setTools(matchKeywords(resumeText, TOOL_KEYWORDS));
        skills.setSoft(matchKeywords(resumeText, SOFT_KEYWORDS));
        if (skills.getCore().isEmpty() && parsed.getSkills() != null && !parsed.getSkills().isBlank()) {
            skills.setCore(Arrays.stream(parsed.getSkills().split("[、,，/]"))
                    .map(String::trim).filter(s -> !s.isEmpty()).limit(8).collect(Collectors.toList()));
        }
        deep.skills = skills;

        deep.experiences = extractExperiencesRule(resumeText);
        deep.strengths = defaultStrengths(parsed);
        deep.weaknesses = defaultWeaknesses(resumeText, parsed);
        deep.suggestions = defaultSuggestions(parsed);
        deep.overallScore = computeCompletenessScore(parsed);
        deep.careerDirection = inferCareerDirection(parsed.getSkills());
        deep.analysisSummary = buildRuleSummary(parsed, deep);
        return deep;
    }

    private ResumeProfileDto buildProfileFromJson(JsonNode json, ParsedResumeResponse parsed) {
        ResumeProfileDto profile = new ResumeProfileDto();
        profile.setName(firstNonBlank(json.path("name").asText(""), parsed.getName()));
        profile.setEmail(firstNonBlank(json.path("email").asText(""), parsed.getEmail()));
        profile.setPhone(firstNonBlank(json.path("phone").asText(""), parsed.getPhone()));
        int years = json.path("yearsOfExperience").asInt(-1);
        profile.setYearsOfExperience(years >= 0 ? years : estimateYears(parsed.getRawText()));
        profile.setEducation(json.path("education").asText(findEducation(parsed.getRawText())));
        profile.setCurrentTitle(json.path("currentTitle").asText(guessCurrentTitle(parsed.getRawText())));
        profile.setSummary(json.path("summary").asText(parsed.getSummary()));
        return profile;
    }

    private ResumeSkillsDto buildSkillsFromJson(JsonNode json, ParsedResumeResponse parsed) {
        ResumeSkillsDto skills = new ResumeSkillsDto();
        skills.setCore(readStringList(json, "coreSkills"));
        skills.setTools(readStringList(json, "toolSkills"));
        skills.setSoft(readStringList(json, "softSkills"));
        if (skills.getCore().isEmpty() && parsed.getSkills() != null) {
            skills.setCore(Arrays.stream(parsed.getSkills().split("[、,，/]"))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }
        return skills;
    }

    private List<ResumeExperienceDto> buildExperiencesFromJson(JsonNode json) {
        JsonNode arr = json.path("experiences");
        if (!arr.isArray()) {
            return List.of();
        }
        List<ResumeExperienceDto> list = new ArrayList<>();
        arr.forEach(node -> {
            ResumeExperienceDto exp = new ResumeExperienceDto();
            exp.setCompany(node.path("company").asText(""));
            exp.setRole(node.path("role").asText(""));
            exp.setDuration(node.path("duration").asText(""));
            exp.setHighlights(node.path("highlights").asText(""));
            if (!exp.getCompany().isBlank() || !exp.getRole().isBlank()) {
                list.add(exp);
            }
        });
        return list;
    }

    private List<String> readStringList(JsonNode json, String field) {
        JsonNode arr = json.path(field);
        if (!arr.isArray()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        arr.forEach(n -> {
            String s = n.asText("").trim();
            if (!s.isEmpty()) {
                list.add(s);
            }
        });
        return list;
    }

    private SkillGap detectSkillGap(Position position, String resumeText, String parsedSkills) {
        String combined = (resumeText + " " + parsedSkills).toLowerCase();
        String jd = (position.getTitle() + " " + position.getDescription()
                + " " + (position.getSkillTags() != null ? position.getSkillTags() : "")).toLowerCase();

        List<String> keywords = new ArrayList<>();
        for (String kw : CORE_SKILL_KEYWORDS) {
            if (jd.contains(kw.toLowerCase())) {
                keywords.add(kw);
            }
        }
        if (keywords.isEmpty()) {
            keywords = List.of("Java", "Vue", "Python", "沟通", "项目");
        }

        List<String> matched = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        for (String kw : keywords) {
            if (combined.contains(kw.toLowerCase())) {
                matched.add(kw);
            } else {
                gaps.add(kw);
            }
        }
        return new SkillGap(matched, gaps);
    }

    private List<ResumeExperienceDto> extractExperiencesRule(String text) {
        List<ResumeExperienceDto> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() < 4 || line.length() > 40) {
                continue;
            }
            if (line.contains("公司") || line.contains("科技") || line.contains("有限") || line.contains("集团")) {
                ResumeExperienceDto exp = new ResumeExperienceDto();
                exp.setCompany(line);
                if (i + 1 < lines.length) {
                    exp.setRole(lines[i + 1].trim());
                }
                Matcher m = YEAR_PATTERN.matcher(text.substring(Math.max(0, text.indexOf(line)), Math.min(text.length(), text.indexOf(line) + 80)));
                if (m.find()) {
                    exp.setDuration(m.group());
                }
                list.add(exp);
                if (list.size() >= 3) {
                    break;
                }
            }
        }
        return list;
    }

    private List<String> defaultStrengths(ParsedResumeResponse parsed) {
        List<String> list = new ArrayList<>();
        if (parsed.getSkills() != null && !parsed.getSkills().isBlank()) {
            list.add("技能栈明确：" + truncate(parsed.getSkills(), 60));
        }
        if (parsed.getEmail() != null && !parsed.getEmail().isBlank()) {
            list.add("联系方式完整，便于 HR 跟进");
        }
        if (parsed.getSummary() != null && parsed.getSummary().length() > 50) {
            list.add("自我评价较充实，能体现个人特点");
        }
        if (list.isEmpty()) {
            list.add("简历结构基本完整，具备进一步沟通的基础");
        }
        return list;
    }

    private List<String> defaultWeaknesses(String resumeText, ParsedResumeResponse parsed) {
        List<String> list = new ArrayList<>();
        if (parsed.getPhone() == null || parsed.getPhone().isBlank()) {
            list.add("缺少手机号码，建议补充以便快速联系");
        }
        if (resumeText.length() < 200) {
            list.add("简历内容偏简略，项目经历描述不够充分");
        }
        if (!resumeText.contains("项目") && !resumeText.toLowerCase().contains("project")) {
            list.add("缺少项目经历描述，难以评估实战能力");
        }
        if (list.isEmpty()) {
            list.add("建议在项目经历中补充量化成果（如性能提升百分比、用户规模等）");
        }
        return list;
    }

    private List<String> defaultSuggestions(ParsedResumeResponse parsed) {
        return List.of(
                "使用 STAR 法则（情境-任务-行动-结果）重写核心项目经历",
                "在技能栏突出与目标岗位最相关的 5-8 项核心技术",
                "补充教育背景、工作年限及期望方向，提升 HR 筛选效率"
        );
    }

    private String buildRuleSummary(ParsedResumeResponse parsed, DeepAnalysis deep) {
        return String.format("候选人「%s」，具备 %s 相关背景，综合完整度评分 %d 分。%s",
                parsed.getName(),
                deep.careerDirection,
                deep.overallScore,
                parsed.getSummary() != null ? truncate(parsed.getSummary(), 80) : "建议补充更详细的项目描述。");
    }

    private int computeCompletenessScore(ParsedResumeResponse parsed) {
        int score = 40;
        if (parsed.getName() != null && !parsed.getName().isBlank()) score += 10;
        if (parsed.getEmail() != null && !parsed.getEmail().isBlank()) score += 10;
        if (parsed.getPhone() != null && !parsed.getPhone().isBlank()) score += 10;
        if (parsed.getSkills() != null && !parsed.getSkills().isBlank()) score += 15;
        if (parsed.getRawText() != null && parsed.getRawText().length() > 300) score += 10;
        if (parsed.getRawText() != null && parsed.getRawText().length() > 800) score += 5;
        return Math.min(95, score);
    }

    private String inferCareerDirection(String skills) {
        if (skills == null) return "通用技术岗位";
        String s = skills.toLowerCase();
        if (s.contains("java") || s.contains("spring")) return "后端开发 / Java 技术栈";
        if (s.contains("vue") || s.contains("react") || s.contains("前端")) return "前端开发 / Web 应用";
        if (s.contains("python") || s.contains("算法")) return "算法 / 数据方向";
        if (s.contains("产品") || s.contains("需求")) return "产品经理 / 业务方向";
        return "互联网技术岗位";
    }

    private Integer estimateYears(String text) {
        if (text == null) return null;
        Matcher m = YEAR_PATTERN.matcher(text);
        int earliest = Integer.MAX_VALUE;
        int latest = 0;
        while (m.find()) {
            try {
                int start = Integer.parseInt(m.group(1));
                earliest = Math.min(earliest, start);
                String endStr = m.group(2);
                int end = endStr.matches("(?i)(至今|present)") ? java.time.Year.now().getValue()
                        : Integer.parseInt(endStr);
                latest = Math.max(latest, end);
            } catch (NumberFormatException ignored) {
            }
        }
        if (earliest == Integer.MAX_VALUE) return null;
        return Math.max(0, (latest > 0 ? latest : java.time.Year.now().getValue()) - earliest);
    }

    private String findEducation(String text) {
        if (text == null) return "未识别";
        Matcher m = EDU_PATTERN.matcher(text);
        return m.find() ? m.group(1) : "未识别";
    }

    private String guessCurrentTitle(String text) {
        if (text == null) return "未识别";
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.contains("工程师") || line.contains("开发") || line.contains("经理")
                    || line.contains("设计师") || line.contains("分析师")) {
                return line.length() > 30 ? line.substring(0, 30) : line;
            }
        }
        return "未识别";
    }

    private List<String> matchKeywords(String text, List<String> keywords) {
        if (text == null) return List.of();
        String lower = text.toLowerCase();
        return keywords.stream()
                .filter(kw -> lower.contains(kw.toLowerCase()))
                .collect(Collectors.toList());
    }

    private String recommendLevel(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 60) return "MEDIUM";
        return "LOW";
    }

    private String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : (b != null ? b : "");
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private static class DeepAnalysis {
        boolean aiPowered;
        ResumeProfileDto profile;
        ResumeSkillsDto skills;
        List<ResumeExperienceDto> experiences = List.of();
        List<String> strengths = List.of();
        List<String> weaknesses = List.of();
        List<String> suggestions = List.of();
        int overallScore;
        String careerDirection;
        String analysisSummary;

        DeepAnalysis(boolean aiPowered) {
            this.aiPowered = aiPowered;
        }
    }

    private record SkillGap(List<String> matched, List<String> gaps) {}
}
