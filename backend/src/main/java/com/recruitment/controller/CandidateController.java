package com.recruitment.controller;

import com.recruitment.dto.*;
import com.recruitment.entity.ApplicationStage;
import com.recruitment.entity.ChannelType;
import com.recruitment.service.AiInterviewService;
import com.recruitment.service.ApplicationService;
import com.recruitment.service.CalendarBookingService;
import com.recruitment.service.InterviewService;
import com.recruitment.service.IntegrationService;
import com.recruitment.service.MockInterviewService;
import com.recruitment.service.ResumeAnalysisService;
import com.recruitment.service.ResumeParseService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/webapi/candidate")
public class CandidateController {

    private final ApplicationService applicationService;
    private final ResumeParseService resumeParseService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final InterviewService interviewService;
    private final MockInterviewService mockInterviewService;
    private final CalendarBookingService calendarBookingService;
    private final IntegrationService integrationService;
    private final AiInterviewService aiInterviewService;

    public CandidateController(ApplicationService applicationService,
                               ResumeParseService resumeParseService,
                               ResumeAnalysisService resumeAnalysisService,
                               InterviewService interviewService,
                               MockInterviewService mockInterviewService,
                               CalendarBookingService calendarBookingService,
                               IntegrationService integrationService,
                               AiInterviewService aiInterviewService) {
        this.applicationService = applicationService;
        this.resumeParseService = resumeParseService;
        this.resumeAnalysisService = resumeAnalysisService;
        this.interviewService = interviewService;
        this.mockInterviewService = mockInterviewService;
        this.calendarBookingService = calendarBookingService;
        this.integrationService = integrationService;
        this.aiInterviewService = aiInterviewService;
    }

    @PostMapping("/apply")
    public ApiResponse<ApplicationResponse> apply(@Valid @RequestBody ApplyRequest request) {
        return ApiResponse.success(applicationService.apply(request));
    }

    @PostMapping("/apply/upload")
    public ApiResponse<ApplicationResponse> applyWithUpload(
            @RequestParam Long positionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) ChannelType channel) throws Exception {
        return ApiResponse.success(applicationService.applyWithResume(positionId, file, channel));
    }

    @PostMapping("/resume/parse")
    public ApiResponse<ParsedResumeResponse> parseResume(@RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.success(resumeParseService.parseFile(file));
    }

    @PostMapping("/resume/analyze")
    public ApiResponse<ResumeAnalysisResponse> analyzeResume(@RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.success(resumeAnalysisService.analyzeFile(file));
    }

    @PostMapping("/resume/analyze-text")
    public ApiResponse<ResumeAnalysisResponse> analyzeResumeText(@RequestBody ResumeAnalyzeTextRequest body) {
        return ApiResponse.success(resumeAnalysisService.analyzeText(body.getText()));
    }

    @GetMapping("/applications")
    public ApiResponse<List<ApplicationResponse>> myApplications() {
        return ApiResponse.success(applicationService.myApplications());
    }

    @GetMapping("/interviews")
    public ApiResponse<List<InterviewResponse>> myInterviews() {
        return ApiResponse.success(interviewService.listForCandidate());
    }

    @GetMapping("/applications/{id}")
    public ApiResponse<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getById(id));
    }

    @GetMapping("/applications/{id}/resume-file")
    public ResponseEntity<Resource> downloadMyResumeFile(@PathVariable Long id) {
        Resource resource = applicationService.getResumeFileResource(id);
        String filename = applicationService.getResumeFileName(id);
        String contentType = applicationService.getResumeContentType(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/applications/{id}/ai-interview")
    public ApiResponse<AiInterviewResponse> aiInterview(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getAiInterviewQuestions(id));
    }

    @PostMapping("/applications/{id}/ai-interview/start")
    public ApiResponse<AiInterviewStartResponse> startAiInterview(@PathVariable Long id) {
        return ApiResponse.success(aiInterviewService.start(id));
    }

    @PostMapping("/applications/{id}/ai-interview/reply")
    public ApiResponse<AiInterviewReplyResponse> replyAiInterview(
            @PathVariable Long id,
            @RequestBody AiInterviewReplyRequest request) {
        return ApiResponse.success(aiInterviewService.reply(id, request));
    }

    @PostMapping("/interviews/{id}/accept")
    public ApiResponse<InterviewResponse> acceptInterview(
            @PathVariable Long id,
            @RequestBody(required = false) InterviewResponseRequest request) {
        return ApiResponse.success(interviewService.acceptInvitation(id, request));
    }

    @PostMapping("/interviews/{id}/decline")
    public ApiResponse<InterviewResponse> declineInterview(
            @PathVariable Long id,
            @RequestBody InterviewResponseRequest request) {
        return ApiResponse.success(interviewService.declineInvitation(id, request));
    }

    @PostMapping("/interviews/{id}/reschedule")
    public ApiResponse<InterviewResponse> rescheduleInterview(
            @PathVariable Long id,
            @RequestBody InterviewResponseRequest request) {
        return ApiResponse.success(interviewService.requestReschedule(id, request));
    }

    @GetMapping("/applications/{id}/mock-interview")
    public ApiResponse<MockInterviewStatusResponse> mockInterviewStatus(@PathVariable Long id) {
        return ApiResponse.success(mockInterviewService.getStatus(id));
    }

    @PostMapping("/applications/{id}/mock-interview/start")
    public ApiResponse<MockInterviewResultResponse> startMockInterview(@PathVariable Long id) {
        return ApiResponse.success(mockInterviewService.startSession(id));
    }

    @PostMapping("/applications/{id}/mock-interview/submit")
    public ApiResponse<MockInterviewResultResponse> submitMockInterview(
            @PathVariable Long id, @RequestBody MockInterviewSubmitRequest request) {
        return ApiResponse.success(mockInterviewService.submitAnswers(id, request.getSessionId(), request.getAnswers()));
    }

    @GetMapping("/applications/{id}/calendar-slots")
    public ApiResponse<List<InterviewSlotResponse>> calendarSlots(@PathVariable Long id) {
        return ApiResponse.success(calendarBookingService.listAvailableSlots(id));
    }

    @PostMapping("/applications/{id}/calendar-slots/{slotId}/book")
    public ApiResponse<InterviewSlotResponse> bookSlot(@PathVariable Long id, @PathVariable Long slotId) {
        return ApiResponse.success(calendarBookingService.bookSlot(id, slotId));
    }

    @PostMapping("/applications/{id}/offer/sign")
    public ApiResponse<OfferResponse> signOffer(@PathVariable Long id) {
        return ApiResponse.success(integrationService.signOffer(id));
    }

    @GetMapping("/applications/{id}/offer")
    public ApiResponse<OfferResponse> getOffer(@PathVariable Long id) {
        return ApiResponse.success(integrationService.getOffer(id));
    }

    @PostMapping("/positions/{positionId}/ask")
    public ApiResponse<AiChatResponse> askAi(@PathVariable Long positionId, @RequestBody MapBody body) {
        return ApiResponse.success(applicationService.askAi(positionId, body.getQuestion()));
    }

    public static class MapBody {
        private String question;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
}
