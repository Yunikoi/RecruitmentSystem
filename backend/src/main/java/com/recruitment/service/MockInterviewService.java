package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.MockInterviewResultResponse;
import com.recruitment.dto.MockInterviewStatusResponse;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.MockInterviewSessionRepository;
import com.recruitment.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MockInterviewService {

    private static final int MAX_MOCK_SESSIONS = 2;

    private final MockInterviewSessionRepository sessionRepository;
    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final AiMatchingService aiMatchingService;
    private final ComplianceService complianceService;

    public MockInterviewService(MockInterviewSessionRepository sessionRepository,
                                ApplicationRepository applicationRepository,
                                PositionRepository positionRepository,
                                AiMatchingService aiMatchingService,
                                ComplianceService complianceService) {
        this.sessionRepository = sessionRepository;
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.aiMatchingService = aiMatchingService;
        this.complianceService = complianceService;
    }

    public MockInterviewStatusResponse getStatus(Long applicationId) {
        Application app = loadOwnedApplication(applicationId);
        long used = sessionRepository.countByApplicationId(applicationId);
        List<MockInterviewSession> history = sessionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        MockInterviewStatusResponse res = new MockInterviewStatusResponse();
        res.setApplicationId(applicationId);
        res.setUsedCount((int) used);
        res.setMaxCount(MAX_MOCK_SESSIONS);
        res.setCanStart(used < MAX_MOCK_SESSIONS && isBeforeAiInterview(app));
        res.setHistory(history.stream().map(s -> {
            MockInterviewResultResponse r = new MockInterviewResultResponse();
            r.setSessionId(s.getId());
            r.setScore(s.getScore());
            r.setFeedback(s.getFeedback());
            r.setCreatedAt(s.getCreatedAt());
            return r;
        }).collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public MockInterviewResultResponse startSession(Long applicationId) {
        Application app = loadOwnedApplication(applicationId);
        if (!isBeforeAiInterview(app)) {
            throw new BusinessException(400, "已进入正式 AI 初试阶段，模拟面试已关闭");
        }
        long used = sessionRepository.countByApplicationId(applicationId);
        if (used >= MAX_MOCK_SESSIONS) {
            throw new BusinessException(400, "模拟面试次数已用完（最多 " + MAX_MOCK_SESSIONS + " 次）");
        }
        Position pos = positionRepository.findById(app.getPositionId())
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        List<String> questions = aiMatchingService.generateInterviewQuestions(
                pos, app.getResumeText(), app.getMatchHighlights());
        if (questions.size() > 3) {
            questions = questions.subList(0, 3);
        }

        MockInterviewSession session = new MockInterviewSession();
        session.setApplicationId(applicationId);
        session.setCandidateId(app.getCandidateId());
        session.setQuestions(String.join("|", questions));
        session = sessionRepository.save(session);

        MockInterviewResultResponse res = new MockInterviewResultResponse();
        res.setSessionId(session.getId());
        res.setQuestions(questions);
        res.setTips("这是模拟面试，请检查麦克风/网络环境。回答后可获得 AI 反馈，不计入正式评估。");
        complianceService.log("MOCK_INTERVIEW_START", "Application", applicationId, "开始模拟面试");
        return res;
    }

    @Transactional
    public MockInterviewResultResponse submitAnswers(Long applicationId, Long sessionId, String answers) {
        loadOwnedApplication(applicationId);
        MockInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(404, "模拟面试不存在"));
        if (!session.getApplicationId().equals(applicationId)) {
            throw new BusinessException(403, "无权提交");
        }
        session.setAnswers(answers != null ? answers : "");
        List<String> qs = Arrays.asList(session.getQuestions().split("\\|"));
        String feedback = aiMatchingService.generateMockFeedback(qs, answers);
        int score = Math.min(95, Math.max(55, 60 + (answers != null ? answers.length() / 20 : 0)));
        session.setFeedback(feedback);
        session.setScore(score);
        sessionRepository.save(session);

        MockInterviewResultResponse res = new MockInterviewResultResponse();
        res.setSessionId(sessionId);
        res.setScore(score);
        res.setFeedback(feedback);
        res.setCreatedAt(session.getCreatedAt());
        complianceService.log("MOCK_INTERVIEW_SUBMIT", "Application", applicationId, "提交模拟面试 score=" + score);
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

    private boolean isBeforeAiInterview(Application app) {
        return app.getStage() == ApplicationStage.APPLIED
                || app.getStage() == ApplicationStage.SCREENING;
    }
}
