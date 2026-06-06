package com.recruitment.controller;

import com.recruitment.dto.*;
import com.recruitment.entity.ApplicationStage;
import com.recruitment.entity.InterviewEvaluation;
import com.recruitment.service.*;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webapi/recruiter")
public class RecruiterController {

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final WorkflowService workflowService;
    private final TalentCrmService talentCrmService;
    private final DuplicateMergeService duplicateMergeService;
    private final IntegrationService integrationService;
    private final InterviewCollabService collabService;
    private final ComplianceService complianceService;

    public RecruiterController(ApplicationService applicationService,
                               InterviewService interviewService,
                               WorkflowService workflowService,
                               TalentCrmService talentCrmService,
                               DuplicateMergeService duplicateMergeService,
                               IntegrationService integrationService,
                               InterviewCollabService collabService,
                               ComplianceService complianceService) {
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.workflowService = workflowService;
        this.talentCrmService = talentCrmService;
        this.duplicateMergeService = duplicateMergeService;
        this.integrationService = integrationService;
        this.collabService = collabService;
        this.complianceService = complianceService;
    }

    @GetMapping("/applications")
    public ApiResponse<List<ApplicationResponse>> listApplications(
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) ApplicationStage stage) {
        return ApiResponse.success(applicationService.listForRecruiter(positionId, stage));
    }

    @GetMapping("/applications/{id}")
    public ApiResponse<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getById(id));
    }

    @PutMapping("/applications/{id}/stage")
    public ApiResponse<ApplicationResponse> updateStage(
            @PathVariable Long id, @RequestBody StageUpdateRequest request) {
        return ApiResponse.success(applicationService.advanceStage(id, request));
    }

    @PostMapping("/interviews")
    public ApiResponse<InterviewResponse> scheduleInterview(@RequestBody InterviewRequest request) {
        return ApiResponse.success(interviewService.schedule(request));
    }

    @GetMapping("/interviews")
    public ApiResponse<List<InterviewResponse>> listInterviews() {
        return ApiResponse.success(interviewService.listForRecruiter());
    }

    @PostMapping("/interviews/{id}/cancel")
    public ApiResponse<InterviewResponse> cancelInterview(@PathVariable Long id) {
        return ApiResponse.success(interviewService.cancelInterview(id));
    }

    @GetMapping("/applications/{id}/meeting-summary")
    public ApiResponse<Map<String, String>> meetingSummary(@PathVariable Long id) {
        return ApiResponse.success(Map.of("summary", interviewService.getMeetingSummary(id)));
    }

    @PostMapping("/evaluations")
    public ApiResponse<InterviewEvaluation> submitEvaluation(@RequestBody EvaluationRequest request) {
        return ApiResponse.success(interviewService.submitEvaluation(request));
    }

    @GetMapping("/applications/{id}/evaluations")
    public ApiResponse<List<InterviewEvaluation>> getEvaluations(@PathVariable Long id) {
        return ApiResponse.success(interviewService.getEvaluations(id));
    }

    @GetMapping("/applications/{id}/collab-code")
    public ApiResponse<Map<String, String>> getCollabCode(@PathVariable Long id) {
        return ApiResponse.success(Map.of("code", collabService.getCode(id)));
    }

    @PutMapping("/applications/{id}/collab-code")
    public ApiResponse<Void> saveCollabCode(@PathVariable Long id, @RequestBody CollabCodeRequest request) {
        collabService.saveCode(id, request.getCode());
        complianceService.log("COLLAB_CODE_SAVE", "Application", id, "保存协同代码");
        return ApiResponse.success(null);
    }

    @GetMapping("/applications/{id}/resume-file")
    public ResponseEntity<Resource> downloadResumeFile(@PathVariable Long id) {
        complianceService.log("DOWNLOAD_RESUME", "Application", id, "下载简历原文件");
        Resource resource = applicationService.getResumeFileResource(id);
        String filename = applicationService.getResumeFileName(id);
        String contentType = applicationService.getResumeContentType(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/talent-pool")
    public ApiResponse<List<ApplicationResponse>> talentPool() {
        return ApiResponse.success(applicationService.talentPool());
    }

    @GetMapping("/talent-pool/match")
    public ApiResponse<List<TalentMatchItem>> matchTalent(@RequestParam Long positionId) {
        return ApiResponse.success(talentCrmService.matchTalentPool(positionId));
    }

    @PostMapping("/talent-pool/activate")
    public ApiResponse<TalentMatchItem> activateTalent(@RequestParam Long applicationId, @RequestParam Long positionId) {
        return ApiResponse.success(talentCrmService.activateTalent(applicationId, positionId));
    }

    @GetMapping("/duplicates")
    public ApiResponse<List<DuplicateGroupResponse>> duplicates() {
        return ApiResponse.success(duplicateMergeService.findDuplicates());
    }

    @PostMapping("/duplicates/merge")
    public ApiResponse<Void> mergeDuplicates(@RequestBody MergeProfilesRequest request) {
        duplicateMergeService.merge(request.getPrimaryId(), request.getMergeIds());
        return ApiResponse.success(null);
    }

    @PutMapping("/positions/{id}/workflow")
    public ApiResponse<List<String>> updateWorkflow(@PathVariable Long id, @RequestBody WorkflowUpdateRequest request) {
        return ApiResponse.success(workflowService.updateWorkflow(id, request.getSteps()));
    }

    @GetMapping("/positions/{id}/workflow")
    public ApiResponse<List<String>> getWorkflow(@PathVariable Long id) {
        return ApiResponse.success(workflowService.getWorkflowByPositionId(id));
    }

    @PostMapping("/applications/{id}/offer")
    public ApiResponse<OfferResponse> createOffer(@PathVariable Long id) {
        return ApiResponse.success(integrationService.createOffer(id));
    }

    @GetMapping("/applications/{id}/offer")
    public ApiResponse<OfferResponse> getOffer(@PathVariable Long id) {
        return ApiResponse.success(integrationService.getOffer(id));
    }

    @PostMapping("/applications/{id}/background-check")
    public ApiResponse<BackgroundCheckResponse> startBgCheck(@PathVariable Long id) {
        return ApiResponse.success(integrationService.initiateBackgroundCheck(id));
    }

    @GetMapping("/applications/{id}/background-check")
    public ApiResponse<BackgroundCheckResponse> getBgCheck(@PathVariable Long id) {
        return ApiResponse.success(integrationService.getBackgroundCheck(id));
    }
}
