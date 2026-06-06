package com.recruitment.controller;

import com.recruitment.dto.*;
import com.recruitment.entity.PositionStatus;
import com.recruitment.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/webapi")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/positions")
    public ApiResponse<List<PositionResponse>> listPositions(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) PositionStatus status) {
        return ApiResponse.success(positionService.listPositions(title, status));
    }

    @GetMapping("/positions/published")
    public ApiResponse<List<PositionResponse>> listPublishedPositions(
            @RequestParam(required = false) String title) {
        return ApiResponse.success(positionService.listPublishedPositions(title));
    }

    @GetMapping("/positions/statistics")
    public ApiResponse<StatisticsResponse> getStatistics() {
        return ApiResponse.success(positionService.getStatistics());
    }

    @GetMapping("/positions/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = positionService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=position-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PostMapping("/positions/import")
    public ApiResponse<ImportResult> importPositions(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(positionService.importFromExcel(file));
    }

    @GetMapping("/public/positions/{id}")
    public ApiResponse<PositionResponse> getPublishedPosition(@PathVariable Long id) {
        return ApiResponse.success(positionService.getPublishedPosition(id));
    }

    @GetMapping("/positions/{id}")
    public ApiResponse<PositionResponse> getPosition(@PathVariable Long id) {
        return ApiResponse.success(positionService.getPosition(id));
    }

    @PostMapping("/positions")
    public ApiResponse<PositionResponse> createPosition(@Valid @RequestBody PositionRequest request) {
        return ApiResponse.success(positionService.createPosition(request));
    }

    @PutMapping("/positions/{id}")
    public ApiResponse<PositionResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request) {
        return ApiResponse.success(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/positions/{id}")
    public ApiResponse<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/positions/{id}/submit")
    public ApiResponse<PositionResponse> submitForApproval(@PathVariable Long id) {
        return ApiResponse.success(positionService.submitForApproval(id));
    }

    @PostMapping("/positions/{id}/approve")
    public ApiResponse<PositionResponse> approvePosition(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.success(positionService.approvePosition(id, request));
    }

    @PostMapping("/positions/{id}/reject")
    public ApiResponse<PositionResponse> rejectPosition(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.success(positionService.rejectPosition(id, request));
    }

    @PostMapping("/positions/{id}/close")
    public ApiResponse<PositionResponse> closePosition(@PathVariable Long id) {
        return ApiResponse.success(positionService.closePosition(id));
    }

    @PostMapping("/positions/copilot")
    public ApiResponse<JdCopilotResponse> generateJd(@RequestBody JdCopilotRequest request) {
        return ApiResponse.success(positionService.generateJd(request));
    }

    @PostMapping("/positions/{id}/remind")
    public ApiResponse<String> remindApproval(@PathVariable Long id) {
        return ApiResponse.success(positionService.remindApproval(id));
    }

    @PostMapping("/positions/{id}/share")
    public ApiResponse<String> shareCandidate(@PathVariable Long id, @RequestBody ShareCandidateRequest request) {
        return ApiResponse.success(positionService.shareCandidate(id, request.getApplicationId(), request.getTargetUser()));
    }
}
