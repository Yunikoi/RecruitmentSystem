package com.recruitment;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.ApplyRequest;
import com.recruitment.dto.ApplicationResponse;
import com.recruitment.dto.StageUpdateRequest;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.PositionRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.service.ApplicationService;
import com.recruitment.service.MatchScoreAsyncService;
import com.recruitment.service.WorkflowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证核心规则在完整业务流中的位置：
 * 投递 → 异步 matchScore → WorkflowService 阶段流转 → HR 推进
 */
@SpringBootTest
@ActiveProfiles("local")
class RecruitmentWorkflowIntegrationTest {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private MatchScoreAsyncService matchScoreAsyncService;
    @Autowired
    private WorkflowService workflowService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void apply_triggersAsyncMatchScore_notSyncOnApply() {
        User candidate = userRepository.findByUsername("candidate").orElseThrow();
        loginAs(candidate);

        Position position = positionRepository.findAll().stream()
                .filter(p -> p.getStatus() == PositionStatus.PUBLISHED)
                .filter(p -> p.getTitle().contains("Go语言"))
                .findFirst()
                .orElseGet(() -> positionRepository.findByStatus(PositionStatus.PUBLISHED).stream()
                        .filter(p -> applicationRepository.findByPositionIdAndCandidateId(p.getId(), candidate.getId()).isEmpty())
                        .findFirst()
                        .orElseThrow());

        applicationRepository.findByPositionIdAndCandidateId(position.getId(), candidate.getId())
                .ifPresent(a -> applicationRepository.delete(a));

        ApplyRequest req = new ApplyRequest();
        req.setPositionId(position.getId());
        req.setName("集成测试候选人");
        req.setEmail("test@example.com");
        req.setResumeText("熟悉 Go、Gin、gRPC、Docker、Kubernetes，3年后端经验。");

        ApplicationResponse response = applicationService.apply(req);

        assertEquals(ApplicationStage.APPLIED, response.getStage());
        assertNull(response.getMatchScore(), "投递同步响应不得包含 matchScore");
        assertTrue(response.getMatchScorePending());

        matchScoreAsyncService.calculateAfterApply(response.getId());

        Application saved = applicationRepository.findById(response.getId()).orElseThrow();
        assertNotNull(saved.getMatchScore(), "异步任务应写入 matchScore");
        assertNotNull(saved.getMatchHighlights());
    }

    @Test
    void applicationStageTransition_mustUseWorkflowService() {
        Application app = applicationRepository.findAll().stream()
                .filter(a -> a.getStage() == ApplicationStage.SCREENING)
                .findFirst()
                .orElseThrow();

        workflowService.transitionApplicationStage(app, ApplicationStage.AI_INTERVIEW, "TEST");
        assertEquals(ApplicationStage.AI_INTERVIEW, app.getStage());

        assertThrows(BusinessException.class, () ->
                workflowService.transitionApplicationStage(app, ApplicationStage.HR_INTERVIEW, "SKIP_STAGES"));
    }

    @Test
    void hrAdvanceStage_followsWorkflowInFullFlow() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        loginAs(admin);

        Application app = applicationRepository.findAll().stream()
                .filter(a -> a.getStage() == ApplicationStage.AI_INTERVIEW)
                .findFirst()
                .orElseGet(() -> applicationRepository.findAll().stream()
                        .filter(a -> a.getStage() == ApplicationStage.SCREENING)
                        .findFirst()
                        .orElseThrow());

        StageUpdateRequest req = new StageUpdateRequest();
        req.setStage(ApplicationStage.BUSINESS_INTERVIEW);
        ApplicationResponse updated = applicationService.advanceStage(app.getId(), req);

        assertEquals(ApplicationStage.BUSINESS_INTERVIEW, updated.getStage());
    }

    @Test
    void positionStatusTransition_mustUseWorkflowService() {
        Position position = new Position();
        position.setTitle("规则测试岗");
        position.setDescription("测试 WorkflowService 岗位流转");
        workflowService.initializePositionAsDraft(position);
        position.setDepartment("技术部");
        Position saved = positionRepository.save(position);

        workflowService.transitionPositionStatus(saved, PositionStatus.PENDING);
        assertEquals(PositionStatus.PENDING, saved.getStatus());

        workflowService.transitionPositionStatus(saved, PositionStatus.PUBLISHED);
        assertEquals(PositionStatus.PUBLISHED, saved.getStatus());

        assertThrows(BusinessException.class, () ->
                workflowService.transitionPositionStatus(saved, PositionStatus.DRAFT));
    }

    private void loginAs(User user) {
        UserContextHolder.set(new UserContext(
                user.getId(), user.getUsername(), user.getDisplayName(),
                user.getDepartment(), user.getRole()));
    }
}
