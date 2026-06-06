package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.*;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private static final Set<String> TERMINAL_STATUSES = Set.of("COMPLETED", "CANCELLED");

    private final InterviewRepository interviewRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final ComplianceService complianceService;
    private final AiMatchingService aiMatchingService;

    public InterviewService(InterviewRepository interviewRepository,
                            InterviewEvaluationRepository evaluationRepository,
                            ApplicationRepository applicationRepository,
                            UserRepository userRepository,
                            PositionRepository positionRepository,
                            ComplianceService complianceService,
                            AiMatchingService aiMatchingService) {
        this.interviewRepository = interviewRepository;
        this.evaluationRepository = evaluationRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.complianceService = complianceService;
        this.aiMatchingService = aiMatchingService;
    }

    @Transactional
    public InterviewResponse schedule(InterviewRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可安排面试");
        }
        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        User interviewer = userRepository.findById(request.getInterviewerId())
                .orElseThrow(() -> new BusinessException(404, "面试官不存在"));
        if (request.getScheduledAt() == null) {
            throw new BusinessException(400, "请填写面试时间");
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new BusinessException(400, "请填写面试地点或会议链接");
        }

        Interview interview = new Interview();
        interview.setApplicationId(request.getApplicationId());
        interview.setInterviewerId(interviewer.getId());
        interview.setInterviewerName(interviewer.getDisplayName());
        interview.setType(request.getType());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setLocation(request.getLocation().trim());
        interview.setQuestions(String.join("|", getStandardQuestions(request.getType())));
        interview.setStatus("PENDING");
        interview.setInvitedById(user.getUserId());
        interview.setInvitedByName(user.getDisplayName());
        interview.setExpireAt(LocalDateTime.now().plusDays(3));

        ApplicationStage stage = switch (request.getType()) {
            case "AI" -> ApplicationStage.AI_INTERVIEW;
            case "BUSINESS" -> ApplicationStage.BUSINESS_INTERVIEW;
            case "HR" -> ApplicationStage.HR_INTERVIEW;
            default -> app.getStage();
        };
        app.setStage(stage);
        app.setStageUpdatedAt(LocalDateTime.now());
        applicationRepository.save(app);

        complianceService.log("SCHEDULE_INTERVIEW", "Application", request.getApplicationId(), request.getType());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse acceptInvitation(Long interviewId, InterviewResponseRequest request) {
        UserContext user = UserContextHolder.require();
        Interview interview = getInterviewForCandidate(interviewId, user);
        ensureCanRespond(interview);
        interview.setStatus("ACCEPTED");
        interview.setResponseNote(request != null && request.getNote() != null ? request.getNote().trim() : null);
        interview.setRespondedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse declineInvitation(Long interviewId, InterviewResponseRequest request) {
        UserContext user = UserContextHolder.require();
        Interview interview = getInterviewForCandidate(interviewId, user);
        ensureCanRespond(interview);
        if (request == null || request.getNote() == null || request.getNote().isBlank()) {
            throw new BusinessException(400, "请填写拒绝原因，便于 HR 安排后续流程");
        }
        interview.setStatus("DECLINED");
        interview.setResponseNote(request.getNote().trim());
        interview.setRespondedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse requestReschedule(Long interviewId, InterviewResponseRequest request) {
        UserContext user = UserContextHolder.require();
        Interview interview = getInterviewForCandidate(interviewId, user);
        ensureCanRespond(interview);
        if (request == null || request.getNote() == null || request.getNote().isBlank()) {
            throw new BusinessException(400, "请说明改期原因");
        }
        String note = request.getNote().trim();
        if (request.getPreferredTime() != null) {
            note += "；期望时间：" + request.getPreferredTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        interview.setStatus("RESCHEDULE_REQUESTED");
        interview.setResponseNote(note);
        interview.setRespondedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse cancelInterview(Long interviewId) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可取消面试邀请");
        }
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(404, "面试邀请不存在"));
        if (TERMINAL_STATUSES.contains(interview.getStatus())) {
            throw new BusinessException(400, "该面试已结束，无法取消");
        }
        interview.setStatus("CANCELLED");
        interview.setRespondedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    public List<InterviewResponse> listForApplicationIds(Collection<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return List.of();
        }
        return interviewRepository.findByApplicationIdInOrderByScheduledAtDesc(applicationIds)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<InterviewResponse> listForCandidate() {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new BusinessException(403, "仅求职者可查看");
        }
        List<Application> apps = applicationRepository.findByCandidateIdOrderByAppliedAtDesc(user.getUserId());
        return enrichResponses(
                interviewRepository.findByApplicationIdInOrderByScheduledAtDesc(
                        apps.stream().map(Application::getId).collect(Collectors.toList())),
                apps);
    }

    public List<InterviewResponse> listForRecruiter() {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.INTERVIEWER) {
            throw new BusinessException(403, "无权查看");
        }
        List<Interview> interviews;
        if (user.getRole() == UserRole.INTERVIEWER) {
            interviews = interviewRepository.findByInterviewerIdOrderByScheduledAtDesc(user.getUserId());
        } else {
            interviews = interviewRepository.findAllByOrderByCreatedAtDesc();
        }
        List<Long> appIds = interviews.stream().map(Interview::getApplicationId).distinct().collect(Collectors.toList());
        List<Application> apps = applicationRepository.findAllById(appIds);
        return enrichResponses(interviews, apps);
    }

    public InterviewResponse toResponse(Interview interview) {
        InterviewResponse r = new InterviewResponse();
        r.setId(interview.getId());
        r.setApplicationId(interview.getApplicationId());
        r.setType(interview.getType());
        r.setTypeLabel(typeLabel(interview.getType()));
        r.setScheduledAt(interview.getScheduledAt());
        r.setLocation(interview.getLocation());
        r.setInterviewerName(interview.getInterviewerName());
        r.setStatus(normalizeStatus(interview.getStatus()));
        r.setStatusLabel(statusLabel(interview.getStatus()));
        r.setCanRespond(canRespond(interview));
        r.setResponseNote(interview.getResponseNote());
        r.setRespondedAt(interview.getRespondedAt());
        r.setInvitedAt(interview.getCreatedAt());
        r.setExpireAt(interview.getExpireAt());
        r.setInvitedByName(interview.getInvitedByName());
        return r;
    }

    @Transactional
    public InterviewEvaluation submitEvaluation(EvaluationRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN
                && user.getRole() != UserRole.INTERVIEWER) {
            throw new BusinessException(403, "仅HR/面试官可提交面评");
        }
        if (request.getApplicationId() == null) {
            throw new BusinessException(400, "缺少 applicationId");
        }
        Long interviewId = request.getInterviewId();
        if (interviewId == null) {
            List<Interview> interviews = interviewRepository.findByApplicationId(request.getApplicationId());
            if (interviews.isEmpty()) {
                Interview created = new Interview();
                created.setApplicationId(request.getApplicationId());
                created.setInterviewerId(user.getUserId());
                created.setInterviewerName(user.getDisplayName());
                created.setType("BUSINESS");
                created.setStatus("COMPLETED");
                created.setScheduledAt(LocalDateTime.now());
                created.setLocation("线上面评");
                interviewId = interviewRepository.save(created).getId();
            } else {
                interviewId = interviews.get(0).getId();
                Interview iv = interviews.get(0);
                if (!TERMINAL_STATUSES.contains(iv.getStatus())) {
                    iv.setStatus("COMPLETED");
                    interviewRepository.save(iv);
                }
            }
        } else {
            interviewRepository.findById(interviewId).ifPresent(iv -> {
                if (!TERMINAL_STATUSES.contains(iv.getStatus())) {
                    iv.setStatus("COMPLETED");
                    interviewRepository.save(iv);
                }
            });
        }
        int t = request.getTechnicalScore() != null ? request.getTechnicalScore() : 0;
        int c = request.getCommunicationScore() != null ? request.getCommunicationScore() : 0;
        int u = request.getCultureScore() != null ? request.getCultureScore() : 0;
        int overall = (t + c + u) / 3;

        InterviewEvaluation eval = new InterviewEvaluation();
        eval.setInterviewId(interviewId);
        eval.setApplicationId(request.getApplicationId());
        eval.setEvaluatorId(user.getUserId());
        eval.setEvaluatorName(user.getDisplayName());
        eval.setTechnicalScore(t);
        eval.setCommunicationScore(c);
        eval.setCultureScore(u);
        eval.setOverallScore(overall);
        eval.setStrengths(request.getStrengths());
        eval.setWeaknesses(request.getWeaknesses());
        eval.setRecommendation(request.getRecommendation());
        eval.setResult(request.getResult());

        return evaluationRepository.save(eval);
    }

    public List<InterviewEvaluation> getEvaluations(Long applicationId) {
        if (complianceService.isBlindReviewEnabled()) {
            UserContext user = UserContextHolder.get();
            if (user != null && user.getRole() == UserRole.INTERVIEWER) {
                long myCount = evaluationRepository.findByApplicationId(applicationId).stream()
                        .filter(e -> e.getEvaluatorId().equals(user.getUserId())).count();
                if (myCount == 0) {
                    return List.of();
                }
                return evaluationRepository.findByApplicationId(applicationId).stream()
                        .filter(e -> e.getEvaluatorId().equals(user.getUserId()))
                        .collect(Collectors.toList());
            }
        }
        return evaluationRepository.findByApplicationId(applicationId);
    }

    public String getMeetingSummary(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        return aiMatchingService.generateMeetingSummary(
                app.getCandidateName(), app.getResumeText(), "视频面试");
    }

    private List<InterviewResponse> enrichResponses(List<Interview> interviews, List<Application> apps) {
        Map<Long, Application> appMap = apps.stream()
                .collect(Collectors.toMap(Application::getId, a -> a, (a, b) -> a));
        Map<Long, Position> posMap = positionRepository.findAllById(
                apps.stream().map(Application::getPositionId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Position::getId, p -> p, (a, b) -> a));

        return interviews.stream().map(iv -> {
            InterviewResponse r = toResponse(iv);
            Application app = appMap.get(iv.getApplicationId());
            if (app != null) {
                r.setCandidateName(app.getCandidateName());
                Position pos = posMap.get(app.getPositionId());
                if (pos != null) {
                    r.setPositionTitle(pos.getTitle());
                }
            }
            return r;
        }).collect(Collectors.toList());
    }

    private Interview getInterviewForCandidate(Long interviewId, UserContext user) {
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new BusinessException(403, "仅求职者可响应面试邀请");
        }
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(404, "面试邀请不存在"));
        Application app = applicationRepository.findById(interview.getApplicationId())
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        if (!app.getCandidateId().equals(user.getUserId())) {
            throw new BusinessException(403, "无权操作该面试邀请");
        }
        return interview;
    }

    private void ensureCanRespond(Interview interview) {
        String status = normalizeStatus(interview.getStatus());
        if (!"PENDING".equals(status)) {
            throw new BusinessException(400, "该邀请已处理，无法重复操作");
        }
        if (interview.getExpireAt() != null && LocalDateTime.now().isAfter(interview.getExpireAt())) {
            throw new BusinessException(400, "邀请已过期，请联系 HR 重新安排");
        }
    }

    private boolean canRespond(Interview interview) {
        String status = normalizeStatus(interview.getStatus());
        if (!"PENDING".equals(status)) {
            return false;
        }
        return interview.getExpireAt() == null || !LocalDateTime.now().isAfter(interview.getExpireAt());
    }

    private String normalizeStatus(String status) {
        if ("SCHEDULED".equals(status)) {
            return "ACCEPTED";
        }
        return status != null ? status : "PENDING";
    }

    private String typeLabel(String type) {
        return Map.of(
                "AI", "AI初试",
                "BUSINESS", "业务面试",
                "HR", "HR面试"
        ).getOrDefault(type, type != null ? type : "面试");
    }

    private String statusLabel(String status) {
        String normalized = normalizeStatus(status);
        return Map.of(
                "PENDING", "待确认",
                "ACCEPTED", "已接受",
                "DECLINED", "已拒绝",
                "RESCHEDULE_REQUESTED", "申请改期",
                "CANCELLED", "已取消",
                "COMPLETED", "已完成"
        ).getOrDefault(normalized, normalized);
    }

    private List<String> getStandardQuestions(String type) {
        return switch (type) {
            case "BUSINESS" -> List.of("技术深度考察", "项目架构设计", "问题解决能力", "团队协作");
            case "HR" -> List.of("职业规划", "薪资期望", "稳定性", "文化匹配");
            default -> List.of("自我介绍", "岗位匹配度", "核心技能");
        };
    }
}
