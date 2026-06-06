package com.recruitment.service;

import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.BackgroundCheckResponse;
import com.recruitment.dto.OfferResponse;
import com.recruitment.entity.*;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.BackgroundCheckRecordRepository;
import com.recruitment.repository.OfferRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IntegrationService {

    private final OfferRecordRepository offerRepository;
    private final BackgroundCheckRecordRepository bgRepository;
    private final ApplicationRepository applicationRepository;
    private final ComplianceService complianceService;
    private final AiMatchingService aiMatchingService;
    private final WorkflowService workflowService;

    public IntegrationService(OfferRecordRepository offerRepository,
                              BackgroundCheckRecordRepository bgRepository,
                              ApplicationRepository applicationRepository,
                              ComplianceService complianceService,
                              AiMatchingService aiMatchingService,
                              WorkflowService workflowService) {
        this.offerRepository = offerRepository;
        this.bgRepository = bgRepository;
        this.applicationRepository = applicationRepository;
        this.complianceService = complianceService;
        this.aiMatchingService = aiMatchingService;
        this.workflowService = workflowService;
    }

    @Transactional
    public OfferResponse createOffer(Long applicationId) {
        requireAdmin();
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        OfferRecord offer = offerRepository.findByApplicationId(applicationId).orElse(new OfferRecord());
        offer.setApplicationId(applicationId);
        offer.setStatus("PENDING_SIGN");
        offer = offerRepository.save(offer);
        workflowService.transitionApplicationStage(app, ApplicationStage.OFFER, "CREATE_OFFER");
        applicationRepository.save(app);
        complianceService.log("CREATE_OFFER", "Application", applicationId, "发起电子签 Offer");
        return toOfferResponse(offer);
    }

    @Transactional
    public OfferResponse signOffer(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        var user = UserContextHolder.require();
        if (!app.getCandidateId().equals(user.getUserId())) {
            throw new BusinessException(403, "仅候选人可签署");
        }
        OfferRecord offer = offerRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new BusinessException(404, "暂无 Offer"));
        offer.setStatus("SIGNED");
        offer.setSignedAt(LocalDateTime.now());
        offerRepository.save(offer);
        complianceService.log("SIGN_OFFER", "Application", applicationId, "候选人签署 Offer");
        return toOfferResponse(offer);
    }

    @Transactional
    public BackgroundCheckResponse initiateBackgroundCheck(Long applicationId) {
        requireAdmin();
        applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));
        BackgroundCheckRecord bg = bgRepository.findByApplicationId(applicationId).orElse(new BackgroundCheckRecord());
        bg.setApplicationId(applicationId);
        bg.setStatus("IN_PROGRESS");
        bg = bgRepository.save(bg);
        complianceService.log("BG_CHECK_START", "Application", applicationId, "发起背调");
        return toBgResponse(bg);
    }

    public BackgroundCheckResponse getBackgroundCheck(Long applicationId) {
        BackgroundCheckRecord bg = bgRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new BusinessException(404, "暂无背调记录"));
        if ("IN_PROGRESS".equals(bg.getStatus())) {
            Application app = applicationRepository.findById(applicationId).orElse(null);
            String summary = aiMatchingService.generateMeetingSummary(
                    app != null ? app.getCandidateName() : "候选人",
                    app != null ? app.getResumeText() : "",
                    "背景调查");
            bg.setReportSummary("【脱敏报告】身份核实：通过；学历核实：通过；"
                    + "工作经历：与简历基本一致。AI 摘要：" + summary);
            bg.setStatus("COMPLETED");
            bgRepository.save(bg);
        }
        return toBgResponse(bg);
    }

    public OfferResponse getOffer(Long applicationId) {
        return offerRepository.findByApplicationId(applicationId)
                .map(this::toOfferResponse)
                .orElseThrow(() -> new BusinessException(404, "暂无 Offer"));
    }

    private void requireAdmin() {
        if (UserContextHolder.require().getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可操作");
        }
    }

    private OfferResponse toOfferResponse(OfferRecord o) {
        OfferResponse r = new OfferResponse();
        r.setApplicationId(o.getApplicationId());
        r.setStatus(o.getStatus());
        r.setSignedAt(o.getSignedAt());
        r.setSignUrl("https://esign.talentflow.local/sign/" + o.getApplicationId());
        return r;
    }

    private BackgroundCheckResponse toBgResponse(BackgroundCheckRecord bg) {
        BackgroundCheckResponse r = new BackgroundCheckResponse();
        r.setApplicationId(bg.getApplicationId());
        r.setStatus(bg.getStatus());
        r.setReportSummary(bg.getReportSummary());
        r.setProvider(bg.getProvider());
        return r;
    }
}
