package com.recruitment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.entity.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiMatchingService {

    private static final Map<String, List<String>> JD_KEYWORDS = Map.of(
            "java", List.of("java", "spring", "spring boot", "微服务", "mysql", "redis"),
            "前端", List.of("vue", "react", "javascript", "typescript", "html", "css", "element"),
            "产品", List.of("需求", "原型", "用户研究", "数据分析", "axure", "prd"),
            "算法", List.of("python", "机器学习", "深度学习", "pytorch", "tensorflow", "算法")
    );

    private final LlmAiService llmAiService;

    public AiMatchingService(LlmAiService llmAiService) {
        this.llmAiService = llmAiService;
    }

    public MatchResult analyze(Position position, String resumeText, String parsedSkills) {
        MatchResult aiResult = analyzeWithLlm(position, resumeText, parsedSkills);
        if (aiResult != null) {
            return aiResult;
        }
        return analyzeWithRules(position, resumeText, parsedSkills);
    }

    public String generateRejectionFeedback(String candidateName, String highlights, List<String> altPositions) {
        String ai = generateRejectionWithLlm(candidateName, highlights, altPositions);
        if (ai != null && !ai.isBlank()) {
            return ai;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("尊敬的").append(candidateName).append("，感谢您参与本次招聘。\n\n");
        sb.append("经过综合评估，本次岗位匹配度未达录用标准。您的亮点：").append(highlights).append("。\n\n");
        if (altPositions != null && !altPositions.isEmpty()) {
            sb.append("我们已将您纳入人才库，并推荐以下更匹配岗位：")
                    .append(String.join("、", altPositions)).append("。\n\n");
        }
        sb.append("期待未来有机会再次合作，祝您职业发展顺利！");
        return sb.toString();
    }

    public String answerQuestion(String question, Position position) {
        String ai = answerWithLlm(question, position);
        if (ai != null && !ai.isBlank()) {
            return ai;
        }
        return answerWithRules(question, position);
    }

    public List<String> generateInterviewQuestions(Position position, String resumeText, String matchHighlights) {
        List<String> ai = interviewQuestionsWithLlm(position, resumeText, matchHighlights);
        if (ai != null && !ai.isEmpty()) {
            return ai;
        }
        return List.of(
                "请用3分钟介绍您最得意的一个项目及您的核心贡献。",
                "针对「" + position.getTitle() + "」，您认为自己的最大优势是什么？",
                "请描述一次您解决复杂技术/业务问题的经历。",
                "您为什么想加入我们？对团队文化有什么期待？",
                "您未来3年的职业规划是什么？"
        );
    }

    private MatchResult analyzeWithLlm(Position position, String resumeText, String parsedSkills) {
        String system = "你是资深互联网招聘 HR，负责简历与岗位 JD 的人岗匹配分析。"
                + "请严格只输出 JSON，不要 markdown，格式："
                + "{\"score\":0-99整数,\"highlights\":\"匹配亮点\",\"risks\":\"潜在风险，无则空字符串\"}";
        String user = """
                【岗位名称】%s
                【岗位描述】%s
                【简历内容】
                %s
                【已解析技能】%s
                请给出匹配分(0-99)、亮点、风险。
                """.formatted(
                position.getTitle(),
                position.getDescription(),
                truncate(resumeText, 6000),
                parsedSkills != null ? parsedSkills : ""
        );
        String reply = llmAiService.chat(system, user);
        JsonNode json = llmAiService.parseJsonFromReply(reply);
        if (json == null) {
            return null;
        }
        int score = json.path("score").asInt(-1);
        if (score < 0) {
            return null;
        }
        score = Math.min(99, Math.max(0, score));
        String highlights = json.path("highlights").asText("简历与岗位具备一定匹配度");
        String risks = json.path("risks").asText("");
        return new MatchResult(score, highlights, risks);
    }

    private String generateRejectionWithLlm(String candidateName, String highlights, List<String> altPositions) {
        String system = "你是温暖专业的 HR，为未通过筛选的候选人写感谢与反馈信，语气真诚，200字以内，纯文本。";
        String user = "候选人：" + candidateName + "\n亮点：" + highlights
                + "\n推荐岗位：" + (altPositions != null ? String.join("、", altPositions) : "暂无");
        return llmAiService.chat(system, user);
    }

    private String answerWithLlm(String question, Position position) {
        String system = "你是 TalentFlow ATS 智能招聘助手，基于岗位信息回答求职者关于薪酬、地点、福利、加班、团队等问题。"
                + "回答简洁友好，150字以内。若 JD 未提及可合理推断互联网行业常见情况并说明。";
        String user = """
                【岗位】%s
                【描述】%s
                【求职者问题】%s
                """.formatted(position.getTitle(), position.getDescription(), question);
        return llmAiService.chat(system, user);
    }

    private List<String> interviewQuestionsWithLlm(Position position, String resumeText, String matchHighlights) {
        String system = "你是 AI 面试官，根据岗位和简历生成 5 道初试问题。"
                + "严格只输出 JSON 数组，例如 [\"问题1\",\"问题2\"] ，不要其他文字。";
        String user = """
                【岗位】%s
                【描述】%s
                【简历摘要】%s
                【匹配亮点】%s
                """.formatted(
                position.getTitle(),
                position.getDescription(),
                truncate(resumeText, 3000),
                matchHighlights != null ? matchHighlights : ""
        );
        String reply = llmAiService.chat(system, user);
        JsonNode json = llmAiService.parseJsonFromReply(reply);
        if (json == null || !json.isArray()) {
            return null;
        }
        List<String> questions = new ArrayList<>();
        json.forEach(node -> {
            String q = node.asText("").trim();
            if (!q.isEmpty()) {
                questions.add(q);
            }
        });
        return questions.isEmpty() ? null : questions;
    }

    private MatchResult analyzeWithRules(Position position, String resumeText, String parsedSkills) {
        String jd = (position.getTitle() + " " + position.getDescription()).toLowerCase();
        String resume = (resumeText + " " + parsedSkills).toLowerCase();

        List<String> requiredKeywords = detectKeywords(jd);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String kw : requiredKeywords) {
            if (resume.contains(kw.toLowerCase())) {
                matched.add(kw);
            } else {
                missing.add(kw);
            }
        }

        int baseScore = requiredKeywords.isEmpty() ? 70 : (matched.size() * 100 / requiredKeywords.size());
        int bonus = countOccurrences(resume, "项目") + countOccurrences(resume, "实习");
        int riskPenalty = countJobHops(resume) * 5;
        int score = Math.min(99, Math.max(30, baseScore + bonus - riskPenalty));

        String highlights = matched.isEmpty()
                ? "简历结构完整，具备基础互联网从业背景"
                : "核心匹配：" + matched.stream().limit(5).collect(Collectors.joining("、"));

        String risks = "";
        if (countJobHops(resume) >= 2) {
            risks = "跳槽较为频繁，建议重点考察稳定性与离职原因";
        } else if (!missing.isEmpty()) {
            risks = "待补充能力：" + missing.stream().limit(3).collect(Collectors.joining("、"));
        }

        return new MatchResult(score, highlights, risks);
    }

    private String answerWithRules(String question, Position position) {
        String q = question.toLowerCase();
        if (q.contains("薪资") || q.contains("工资") || q.contains("薪酬")) {
            return "该岗位薪酬范围为互联网行业中位水平，具体根据能力与面试表现面议，14-16薪+期权激励。";
        }
        if (q.contains("地点") || q.contains("远程") || q.contains("办公")) {
            return "主要办公地点为北京/上海/杭州，支持 hybrid 混合办公，核心协作日需到岗。";
        }
        if (q.contains("福利") || q.contains("五险一金")) {
            return "提供六险一金、年度体检、带薪年假、免费三餐、健身房及员工旅游等互联网标准福利。";
        }
        if (q.contains("加班")) {
            return "我们倡导高效工作，不鼓励无效加班。项目关键期会有适度加班，并提供调休与补贴。";
        }
        String desc = position.getDescription();
        int len = desc != null ? Math.min(100, desc.length()) : 0;
        return "关于「" + position.getTitle() + "」：" + (desc != null ? desc.substring(0, len) : "") + "... 如需了解更多，欢迎继续提问。";
    }

    private List<String> detectKeywords(String jd) {
        for (Map.Entry<String, List<String>> entry : JD_KEYWORDS.entrySet()) {
            if (jd.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return List.of("沟通", "协作", "学习", "项目");
    }

    private int countOccurrences(String text, String word) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            count++;
            idx += word.length();
        }
        return count;
    }

    private int countJobHops(String resume) {
        return countOccurrences(resume, "至今") + countOccurrences(resume, "present") - 1;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    public String generateMockFeedback(List<String> questions, String answers) {
        String system = "你是面试教练，根据模拟面试问答给出简短反馈（150字内），包含优点和1-2条改进建议。";
        String user = "问题：" + String.join("；", questions) + "\n回答：" + (answers != null ? answers : "");
        String reply = llmAiService.chat(system, user);
        if (reply != null && !reply.isBlank()) {
            return reply.trim();
        }
        return "整体表达尚可，建议在回答中增加 STAR 结构（情境-任务-行动-结果），并补充量化成果。";
    }

    public JdCopilotResult generateJd(String brief) {
        String system = "你是 HR JD 专家。根据用户简述生成岗位 JSON："
                + "{\"title\":\"岗位名\",\"description\":\"含岗位职责/任职要求/加分项，换行分隔\",\"skillTags\":\"逗号分隔技能标签\",\"positionType\":\"TECH|SENIOR|GENERAL\"}";
        String reply = llmAiService.chat(system, "请生成 JD：" + brief);
        JsonNode json = llmAiService.parseJsonFromReply(reply);
        if (json != null) {
            return new JdCopilotResult(
                    json.path("title").asText("新岗位"),
                    json.path("description").asText(brief),
                    json.path("skillTags").asText(""),
                    json.path("positionType").asText("GENERAL")
            );
        }
        return new JdCopilotResult("新岗位", "岗位职责：\n" + brief + "\n\n任职要求：\n" + brief,
                "Java,Spring", "GENERAL");
    }

    public String generateMeetingSummary(String candidateName, String resumeText, String interviewType) {
        String system = "你是面试记录助手，根据信息生成面试摘要（200字内）：技术亮点、离职/跳槽原因推测、软实力评估。";
        String user = "候选人：" + candidateName + "\n类型：" + interviewType + "\n简历摘要：" + truncate(resumeText, 500);
        String reply = llmAiService.chat(system, user);
        if (reply != null && !reply.isBlank()) {
            return reply.trim();
        }
        return "候选人具备相关技术栈经验，沟通表达清晰，文化匹配度待进一步评估。建议关注项目深度与团队协作案例。";
    }

    public String aiInterviewAcknowledge(String question, String answer, String positionTitle) {
        String system = "你是专业 AI 面试官，语气自然口语化。候选人刚回答完一题，"
                + "请用 1-2 句简短回应（可肯定亮点或温和追问一个点），不要出下一题，80字以内。";
        String user = "岗位：" + positionTitle + "\n问题：" + question + "\n候选人回答：" + answer;
        String reply = llmAiService.chat(system, user);
        if (reply != null && !reply.isBlank()) {
            return reply.trim();
        }
        return "好的，感谢您的分享。";
    }

    public String aiInterviewSummary(String positionTitle, List<String> transcript) {
        String system = "你是 AI 面试官，初试刚结束。根据对话记录给出 120 字以内的综合点评："
                + "优势、待提升点、是否建议进入下一轮。纯文本，不要 JSON。";
        String user = "岗位：" + positionTitle + "\n\n对话记录：\n" + String.join("\n", transcript);
        String reply = llmAiService.chat(system, user);
        if (reply != null && !reply.isBlank()) {
            return reply.trim();
        }
        return "整体表现良好，表达较清晰，建议结合项目细节进一步评估。";
    }

    public int aiInterviewScore(String summary) {
        String system = "根据面试综合点评，只输出一个 0-100 的整数分数，不要其他文字。";
        String reply = llmAiService.chat(system, summary);
        if (reply != null) {
            try {
                String num = reply.replaceAll("[^0-9]", "");
                if (!num.isEmpty()) {
                    return Math.min(100, Math.max(0, Integer.parseInt(num.substring(0, Math.min(3, num.length())))));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 75;
    }

    public record MatchResult(int score, String highlights, String risks) {}

    public record JdCopilotResult(String title, String description, String skillTags, String positionType) {}
}
