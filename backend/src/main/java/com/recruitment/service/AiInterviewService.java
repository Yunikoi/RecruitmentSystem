package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.*;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiInterviewService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final AiMatchingService aiMatchingService;
    private final ComplianceService complianceService;
    private final WorkflowService workflowService;

    public AiInterviewService(ApplicationRepository applicationRepository,
                              PositionRepository positionRepository,
                              AiMatchingService aiMatchingService,
                              ComplianceService complianceService,
                              WorkflowService workflowService) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.aiMatchingService = aiMatchingService;
        this.complianceService = complianceService;
        this.workflowService = workflowService;
    }

    public AiInterviewStartResponse start(Long applicationId) {
        Application app = loadOwnedApplication(applicationId);
        if (app.getStage() != ApplicationStage.SCREENING && app.getStage() != ApplicationStage.AI_INTERVIEW) {
            throw new BusinessException(400, "当前阶段不可进入 AI 初试");
        }
        Position pos = positionRepository.findById(app.getPositionId())
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        List<String> questions = aiMatchingService.generateInterviewQuestions(
                pos, app.getResumeText(), app.getMatchHighlights());

        Session session = new Session();
        session.applicationId = applicationId;
        session.candidateId = app.getCandidateId();
        session.positionTitle = pos.getTitle();
        session.questions = questions;
        session.currentIndex = 0;
        session.transcript = new ArrayList<>();
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, session);

        String greeting = "您好，我是 AI 面试官。接下来共 " + questions.size()
                + " 个问题，您可以用语音或文字回答。第一题：";
        String firstQ = questions.get(0);
        session.transcript.add("AI：" + greeting + firstQ);

        AiInterviewStartResponse res = new AiInterviewStartResponse();
        res.setSessionId(sessionId);
        res.setAiMessage(greeting + firstQ);
        res.setQuestionIndex(1);
        res.setTotalQuestions(questions.size());
        res.setTips("请允许浏览器使用麦克风；也可直接打字回答。每题建议 2-3 分钟。");
        complianceService.log("AI_INTERVIEW_START", "Application", applicationId, "开始语音AI初试");
        return res;
    }

    @Transactional
    public AiInterviewReplyResponse reply(Long applicationId, AiInterviewReplyRequest request) {
        if (request.getSessionId() == null || request.getAnswer() == null || request.getAnswer().isBlank()) {
            throw new BusinessException(400, "请提供回答内容");
        }
        Application app = loadOwnedApplication(applicationId);
        Session session = sessions.get(request.getSessionId());
        if (session == null || !session.applicationId.equals(applicationId)) {
            throw new BusinessException(400, "面试会话已过期，请重新开始");
        }

        String answer = request.getAnswer().trim();
        String currentQuestion = session.questions.get(session.currentIndex);
        session.transcript.add("候选人：" + answer);

        AiInterviewReplyResponse res = new AiInterviewReplyResponse();
        res.setTotalQuestions(session.questions.size());

        String ack = aiMatchingService.aiInterviewAcknowledge(currentQuestion, answer, session.positionTitle);
        session.currentIndex++;

        if (session.currentIndex >= session.questions.size()) {
            String summary = aiMatchingService.aiInterviewSummary(session.positionTitle, session.transcript);
            int score = aiMatchingService.aiInterviewScore(summary);
            session.transcript.add("AI：" + ack);
            session.transcript.add("AI：面试结束。" + summary);

            String finalMsg = ack + " 以上是全部问题，感谢您的回答。" + summary;
            res.setAiMessage(finalMsg);
            res.setQuestionIndex(session.questions.size());
            res.setFinished(true);
            res.setScore(score);
            res.setSummary(summary);

            app.setAiInterviewScore(score);
            app.setAiInterviewFeedback(summary);
            app.setAiInterviewPass(score >= 60);
            app.setAiInterviewAt(LocalDateTime.now());
            if (app.getStage() == ApplicationStage.SCREENING) {
                workflowService.transitionApplicationStage(app, ApplicationStage.AI_INTERVIEW, "AI_INTERVIEW_FINISH");
            }
            applicationRepository.save(app);
            sessions.remove(request.getSessionId());
            complianceService.log("AI_INTERVIEW_FINISH", "Application", applicationId,
                    "score=" + score + ", pass=" + (score >= 60) + ", summary=" + truncate(summary, 200));
            return res;
        }

        String nextQ = session.questions.get(session.currentIndex);
        String msg = ack + " 下一题：" + nextQ;
        session.transcript.add("AI：" + msg);
        res.setAiMessage(msg);
        res.setQuestionIndex(session.currentIndex + 1);
        res.setFinished(false);
        return res;
    }

    private Application loadOwnedApplication(Long applicationId) {
        UserContext user = UserContextHolder.require();
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        if (!app.getCandidateId().equals(user.getUserId())) {
            throw new BusinessException(403, "无权访问");
        }
        return app;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private static class Session {
        Long applicationId;
        Long candidateId;
        String positionTitle;
        List<String> questions;
        int currentIndex;
        List<String> transcript;
    }
}
