package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.InterviewSlotResponse;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.InterviewRepository;
import com.recruitment.repository.InterviewSlotRepository;
import com.recruitment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarBookingService {

    private final InterviewSlotRepository slotRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final ComplianceService complianceService;

    public CalendarBookingService(InterviewSlotRepository slotRepository,
                                  ApplicationRepository applicationRepository,
                                  InterviewRepository interviewRepository,
                                  UserRepository userRepository,
                                  ComplianceService complianceService) {
        this.slotRepository = slotRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.complianceService = complianceService;
    }

    public List<InterviewSlotResponse> listAvailableSlots(Long applicationId) {
        Application app = loadOwnedApplication(applicationId);
        if (!isBookableStage(app.getStage())) {
            return List.of();
        }
        return slotRepository.findByPositionIdAndBookedFalseOrderByStartTimeAsc(app.getPositionId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public InterviewSlotResponse bookSlot(Long applicationId, Long slotId) {
        Application app = loadOwnedApplication(applicationId);
        if (!isBookableStage(app.getStage())) {
            throw new BusinessException(400, "当前阶段不支持自助预约，请等待 HR 安排");
        }
        InterviewSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(404, "时段不存在"));
        if (Boolean.TRUE.equals(slot.getBooked())) {
            throw new BusinessException(400, "该时段已被预约");
        }
        if (!slot.getPositionId().equals(app.getPositionId())) {
            throw new BusinessException(400, "时段与岗位不匹配");
        }
        slot.setBooked(true);
        slot.setBookedApplicationId(applicationId);
        slotRepository.save(slot);

        User interviewer = userRepository.findById(slot.getInterviewerId()).orElse(null);
        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setInterviewerId(slot.getInterviewerId());
        interview.setInterviewerName(interviewer != null ? interviewer.getDisplayName() : slot.getInterviewerName());
        interview.setType(app.getStage() == ApplicationStage.HR_INTERVIEW ? "HR" : "BUSINESS");
        interview.setScheduledAt(slot.getStartTime());
        interview.setLocation("自助预约 · 线上会议室");
        interview.setStatus("ACCEPTED");
        interview.setRespondedAt(LocalDateTime.now());
        interviewRepository.save(interview);

        complianceService.log("BOOK_INTERVIEW_SLOT", "Application", applicationId,
                "预约时段 " + slot.getStartTime());
        return toResponse(slot);
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

    private boolean isBookableStage(ApplicationStage stage) {
        return stage == ApplicationStage.BUSINESS_INTERVIEW || stage == ApplicationStage.HR_INTERVIEW;
    }

    private InterviewSlotResponse toResponse(InterviewSlot slot) {
        InterviewSlotResponse r = new InterviewSlotResponse();
        r.setId(slot.getId());
        r.setInterviewerName(slot.getInterviewerName());
        r.setStartTime(slot.getStartTime());
        r.setEndTime(slot.getEndTime());
        r.setBooked(Boolean.TRUE.equals(slot.getBooked()));
        return r;
    }
}
