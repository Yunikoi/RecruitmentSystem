package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.*;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.PositionRepository;
import com.recruitment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final ResumeParseService resumeParseService;
    private final AiMatchingService aiMatchingService;
    private final ResumeFileStorageService resumeFileStorageService;
    private final InterviewService interviewService;
    private final ComplianceService complianceService;
    private final WorkflowService workflowService;
    private final MatchScoreAsyncService matchScoreAsyncService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              PositionRepository positionRepository,
                              UserRepository userRepository,
                              ResumeParseService resumeParseService,
                              AiMatchingService aiMatchingService,
                              ResumeFileStorageService resumeFileStorageService,
                              InterviewService interviewService,
                              ComplianceService complianceService,
                              WorkflowService workflowService,
                              MatchScoreAsyncService matchScoreAsyncService) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.resumeParseService = resumeParseService;
        this.aiMatchingService = aiMatchingService;
        this.resumeFileStorageService = resumeFileStorageService;
        this.interviewService = interviewService;
        this.complianceService = complianceService;
        this.workflowService = workflowService;
        this.matchScoreAsyncService = matchScoreAsyncService;
    }

    @Transactional
    public ApplicationResponse apply(ApplyRequest request) {
        UserContext user = UserContextHolder.require();
        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        if (position.getStatus() != PositionStatus.PUBLISHED) {
            throw new BusinessException(400, "该岗位暂未开放投递");
        }
        applicationRepository.findByPositionIdAndCandidateId(request.getPositionId(), user.getUserId())
                .ifPresent(a -> { throw new BusinessException(400, "您已投递过该岗位"); });

        String resumeText = request.getResumeText() != null ? request.getResumeText() : "";
        ParsedResumeResponse parsed = resumeParseService.parseText(resumeText);

        Application app = new Application();
        app.setPositionId(request.getPositionId());
        app.setCandidateId(user.getUserId());
        app.setCandidateName(request.getName() != null && !request.getName().isBlank() ? request.getName() : parsed.getName());
        app.setCandidateEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : parsed.getEmail());
        app.setCandidatePhone(request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone() : parsed.getPhone());
        app.setChannel(request.getChannel() != null ? request.getChannel() : ChannelType.OFFICIAL);
        app.setResumeText(resumeText);
        app.setParsedSkills(parsed.getSkills());
        workflowService.transitionApplicationStage(app, ApplicationStage.APPLIED, "APPLY");

        Application saved = applicationRepository.save(app);
        matchScoreAsyncService.calculateAfterApply(saved.getId());
        return toResponse(saved, position);
    }

    @Transactional
    public ApplicationResponse applyWithResume(Long positionId, MultipartFile file, ChannelType channel) throws Exception {
        byte[] fileBytes = file.getBytes();
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        ParsedResumeResponse parsed = resumeParseService.parseBytes(fileBytes, filename);
        ResumeFileStorageService.StoredFile stored = resumeFileStorageService.storeBytes(fileBytes, filename, contentType);
        ApplyRequest req = new ApplyRequest();
        req.setPositionId(positionId);
        req.setName(parsed.getName());
        req.setEmail(parsed.getEmail());
        req.setPhone(parsed.getPhone());
        req.setResumeText(parsed.getRawText());
        req.setChannel(channel != null ? channel : ChannelType.OFFICIAL);

        UserContext user = UserContextHolder.require();
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        if (position.getStatus() != PositionStatus.PUBLISHED) {
            throw new BusinessException(400, "该岗位暂未开放投递");
        }
        applicationRepository.findByPositionIdAndCandidateId(positionId, user.getUserId())
                .ifPresent(a -> { throw new BusinessException(400, "您已投递过该岗位"); });

        String resumeText = req.getResumeText() != null ? req.getResumeText() : "";

        Application app = new Application();
        app.setPositionId(positionId);
        app.setCandidateId(user.getUserId());
        app.setCandidateName(req.getName() != null && !req.getName().isBlank() ? req.getName() : parsed.getName());
        app.setCandidateEmail(req.getEmail() != null && !req.getEmail().isBlank() ? req.getEmail() : parsed.getEmail());
        app.setCandidatePhone(req.getPhone() != null && !req.getPhone().isBlank() ? req.getPhone() : parsed.getPhone());
        app.setChannel(req.getChannel() != null ? req.getChannel() : ChannelType.OFFICIAL);
        app.setResumeText(resumeText);
        app.setParsedSkills(parsed.getSkills());
        app.setResumeFileName(stored.originalName());
        app.setResumeStoredName(stored.storedName());
        app.setResumeContentType(stored.contentType());
        workflowService.transitionApplicationStage(app, ApplicationStage.APPLIED, "APPLY_UPLOAD");

        Application saved = applicationRepository.save(app);
        matchScoreAsyncService.calculateAfterApply(saved.getId());
        return toResponse(saved, position);
    }

    public List<ApplicationResponse> myApplications() {
        UserContext user = UserContextHolder.require();
        List<Application> apps = applicationRepository.findByCandidateIdOrderByAppliedAtDesc(user.getUserId())
                .stream().filter(a -> a.getMergedIntoId() == null).collect(Collectors.toList());
        List<ApplicationResponse> responses = apps.stream().map(this::toResponse).collect(Collectors.toList());
        attachInterviews(responses);
        return responses;
    }

    public List<ApplicationResponse> listForRecruiter(Long positionId, ApplicationStage stage) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.INTERVIEWER) {
            throw new BusinessException(403, "无权查看");
        }
        List<Application> apps;
        if (positionId != null && stage != null) {
            apps = applicationRepository.findByPositionIdAndStage(positionId, stage);
        } else if (positionId != null) {
            apps = applicationRepository.findByPositionIdOrderByMatchScoreDesc(positionId);
        } else if (stage != null) {
            apps = applicationRepository.findByStage(stage);
        } else {
            apps = applicationRepository.findAll();
        }
        apps = apps.stream().filter(a -> a.getMergedIntoId() == null).collect(Collectors.toList());
        List<ApplicationResponse> responses = apps.stream().map(this::toResponse).collect(Collectors.toList());
        attachInterviews(responses);
        return responses.stream().map(this::applyBlindIfNeeded).collect(Collectors.toList());
    }

    public ApplicationResponse getById(Long id) {
        Application app = findById(id);
        checkAccess(app);
        ApplicationResponse response = toResponse(app);
        UserContext user = UserContextHolder.require();
        if (user.getRole() == UserRole.CANDIDATE || user.getRole() == UserRole.ADMIN
                || user.getRole() == UserRole.INTERVIEWER) {
            response.setInterviews(interviewService.listForApplicationIds(List.of(app.getId())));
        }
        return response;
    }

    @Transactional
    public ApplicationResponse advanceStage(Long id, StageUpdateRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可操作");
        }
        Application app = findById(id);
        workflowService.transitionApplicationStage(app, request.getStage(), "HR_ADVANCE");

        if (request.getStage() == ApplicationStage.REJECTED) {
            Position pos = positionRepository.findById(app.getPositionId()).orElse(null);
            List<String> alts = findAlternativePositions(app);
            app.setAiFeedback(aiMatchingService.generateRejectionFeedback(
                    app.getCandidateName(), app.getMatchHighlights(), alts));
            app.setRecommendedPositions(String.join("、", alts));
            app.setInTalentPool(true);
        }
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse deduplicateCheck(String email) {
        List<Application> existing = applicationRepository.findByCandidateEmail(email);
        if (existing.size() > 1) {
            return toResponse(existing.get(0));
        }
        throw new BusinessException(404, "未发现重复简历");
    }

    public AiInterviewResponse getAiInterviewQuestions(Long applicationId) {
        Application app = findById(applicationId);
        checkAccess(app);
        Position pos = positionRepository.findById(app.getPositionId()).orElseThrow();
        List<String> questions = aiMatchingService.generateInterviewQuestions(
                pos, app.getResumeText(), app.getMatchHighlights());
        String tips = "AI面试官提示：请保持环境安静，回答简洁有条理，每题建议2-3分钟。";
        return new AiInterviewResponse(questions, tips);
    }

    public AiChatResponse askAi(Long positionId, String question) {
        Position pos = positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        String answer = aiMatchingService.answerQuestion(question, pos);
        return new AiChatResponse(answer, List.of("薪资范围？", "工作地点？", "有哪些福利？", "加班情况？"));
    }

    public List<ApplicationResponse> talentPool() {
        return applicationRepository.findByInTalentPoolTrueOrderByMatchScoreDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public org.springframework.core.io.Resource getResumeFileResource(Long id) {
        Application app = findById(id);
        UserContext user = UserContextHolder.require();
        if (user.getRole() == UserRole.CANDIDATE) {
            if (!app.getCandidateId().equals(user.getUserId())) {
                throw new BusinessException(403, "无权查看");
            }
        } else if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.INTERVIEWER
                && user.getRole() != UserRole.EXECUTIVE) {
            throw new BusinessException(403, "无权查看简历原文件");
        }
        return resumeFileStorageService.loadAsResource(app.getResumeStoredName());
    }

    private ApplicationResponse applyBlindIfNeeded(ApplicationResponse r) {
        if (!complianceService.isBlindHiringEnabled()) {
            return r;
        }
        UserContext user = UserContextHolder.get();
        if (user == null || user.getRole() == UserRole.CANDIDATE) {
            return r;
        }
        if (r.getStage() == ApplicationStage.APPLIED || r.getStage() == ApplicationStage.SCREENING) {
            r.setCandidateName(complianceService.anonymizeName(r.getCandidateName()));
            r.setCandidateEmail("***@***.com");
            r.setCandidatePhone("***********");
            if (r.getResumeText() != null) {
                r.setResumeText("[盲筛模式] 已隐藏姓名/联系方式/学校信息，仅保留技能与经历画像。");
            }
        }
        return r;
    }

    public String getResumeFileName(Long id) {
        Application app = findById(id);
        return app.getResumeFileName() != null ? app.getResumeFileName() : "resume";
    }

    public String getResumeContentType(Long id) {
        Application app = findById(id);
        return app.getResumeContentType() != null ? app.getResumeContentType() : "application/octet-stream";
    }

    private List<String> findAlternativePositions(Application app) {
        return positionRepository.findByStatus(PositionStatus.PUBLISHED).stream()
                .filter(p -> !p.getId().equals(app.getPositionId()))
                .limit(2)
                .map(Position::getTitle)
                .collect(Collectors.toList());
    }

    private void attachInterviews(List<ApplicationResponse> responses) {
        if (responses.isEmpty()) {
            return;
        }
        List<Long> ids = responses.stream().map(ApplicationResponse::getId).collect(Collectors.toList());
        Map<Long, List<InterviewResponse>> grouped = interviewService.listForApplicationIds(ids).stream()
                .collect(Collectors.groupingBy(InterviewResponse::getApplicationId));
        responses.forEach(r -> r.setInterviews(grouped.getOrDefault(r.getId(), List.of())));
    }

    private Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
    }

    private void checkAccess(Application app) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() == UserRole.CANDIDATE && !app.getCandidateId().equals(user.getUserId())) {
            throw new BusinessException(403, "无权查看");
        }
    }

    private ApplicationResponse toResponse(Application app) {
        Position pos = positionRepository.findById(app.getPositionId()).orElse(null);
        return toResponse(app, pos);
    }

    private ApplicationResponse toResponse(Application app, Position pos) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(app.getId());
        r.setPositionId(app.getPositionId());
        r.setPositionTitle(pos != null ? pos.getTitle() : "");
        r.setDepartment(pos != null ? pos.getDepartment() : "");
        r.setCandidateId(app.getCandidateId());
        r.setCandidateName(app.getCandidateName());
        r.setCandidateEmail(app.getCandidateEmail());
        r.setCandidatePhone(app.getCandidatePhone());
        r.setStage(app.getStage());
        r.setStageLabel(stageLabel(app.getStage()));
        r.setChannel(app.getChannel());
        r.setChannelLabel(channelLabel(app.getChannel()));
        r.setMatchScore(app.getMatchScore());
        r.setMatchHighlights(app.getMatchHighlights());
        r.setMatchRisks(app.getMatchRisks());
        r.setResumeText(app.getResumeText());
        r.setParsedSkills(app.getParsedSkills());
        r.setResumeFileName(app.getResumeFileName());
        r.setResumeContentType(app.getResumeContentType());
        r.setHasResumeFile(app.getResumeStoredName() != null && !app.getResumeStoredName().isBlank());
        r.setAiFeedback(app.getAiFeedback());
        r.setAiInterviewScore(app.getAiInterviewScore());
        r.setAiInterviewFeedback(app.getAiInterviewFeedback());
        r.setAiInterviewPass(app.getAiInterviewPass());
        r.setAiInterviewAt(app.getAiInterviewAt());
        r.setRecommendedPositions(app.getRecommendedPositions());
        r.setInTalentPool(app.getInTalentPool());
        r.setAppliedAt(app.getAppliedAt());
        r.setStageUpdatedAt(app.getStageUpdatedAt());
        return r;
    }

    private String stageLabel(ApplicationStage stage) {
        return Map.of(
                ApplicationStage.APPLIED, "已投递",
                ApplicationStage.SCREENING, "简历初筛",
                ApplicationStage.AI_INTERVIEW, "AI初试",
                ApplicationStage.BUSINESS_INTERVIEW, "业务面试",
                ApplicationStage.HR_INTERVIEW, "HR面试",
                ApplicationStage.OFFER, "Offer中",
                ApplicationStage.HIRED, "已录用",
                ApplicationStage.REJECTED, "未通过"
        ).getOrDefault(stage, stage.name());
    }

    private String channelLabel(ChannelType channel) {
        return Map.of(
                ChannelType.OFFICIAL, "官网",
                ChannelType.BOSS, "Boss直聘",
                ChannelType.LIEPIN, "猎聘",
                ChannelType.LINKEDIN, "LinkedIn",
                ChannelType.REFERRAL, "内推",
                ChannelType.HEADHUNTER, "猎头",
                ChannelType.CAMPUS, "校招"
        ).getOrDefault(channel, channel.name());
    }
}
