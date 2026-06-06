package com.recruitment.service;

import com.recruitment.context.UserContext;
import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.*;
import com.recruitment.entity.Position;
import com.recruitment.entity.PositionStatus;
import com.recruitment.entity.UserRole;
import com.recruitment.exception.BusinessException;
import com.recruitment.mapper.PositionMapper;
import com.recruitment.repository.PositionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final AiMatchingService aiMatchingService;
    private final ComplianceService complianceService;
    private final WorkflowService workflowService;

    public PositionService(PositionRepository positionRepository,
                           AiMatchingService aiMatchingService,
                           ComplianceService complianceService,
                           WorkflowService workflowService) {
        this.positionRepository = positionRepository;
        this.aiMatchingService = aiMatchingService;
        this.complianceService = complianceService;
        this.workflowService = workflowService;
    }

    public List<PositionResponse> listPositions(String title, PositionStatus status) {
        UserContext user = UserContextHolder.get();
        if (user == null) {
            if (status != null && status != PositionStatus.PUBLISHED) {
                return List.of();
            }
            return listPublishedPositions(title);
        }
        List<Position> positions;
        if (user.getRole() == UserRole.DEPARTMENT) {
            positions = queryDepartmentPositions(user.getUserId(), title, status);
        } else {
            positions = queryAllPositions(title, status);
        }
        return positions.stream().map(PositionMapper::toResponse).collect(Collectors.toList());
    }

    public List<PositionResponse> listPublishedPositions(String title) {
        List<Position> positions;
        if (title != null && !title.isBlank()) {
            positions = positionRepository.findByTitleContainingIgnoreCaseAndStatus(title.trim(), PositionStatus.PUBLISHED);
        } else {
            positions = positionRepository.findByStatus(PositionStatus.PUBLISHED);
        }
        return positions.stream().map(PositionMapper::toResponse).collect(Collectors.toList());
    }

    public PositionResponse getPublishedPosition(Long id) {
        Position position = findById(id);
        if (position.getStatus() != PositionStatus.PUBLISHED) {
            throw new BusinessException(404, "岗位不存在或未发布");
        }
        return PositionMapper.toResponse(position);
    }

    public PositionResponse getPosition(Long id) {
        Position position = findById(id);
        checkViewPermission(position);
        return PositionMapper.toResponse(position);
    }

    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.DEPARTMENT) {
            throw new BusinessException(403, "仅部门账号可提交岗位");
        }
        Position position = new Position();
        position.setTitle(request.getTitle().trim());
        position.setDescription(request.getDescription().trim());
        position.setStatus(PositionStatus.DRAFT);
        position.setCreatedById(user.getUserId());
        position.setCreatedByName(user.getDisplayName());
        position.setDepartment(user.getDepartment());
        applyOptionalFields(position, request);
        Position saved = positionRepository.save(position);
        complianceService.log("CREATE_POSITION", "Position", saved.getId(), saved.getTitle());
        return PositionMapper.toResponse(saved);
    }

    @Transactional
    public PositionResponse updatePosition(Long id, PositionRequest request) {
        UserContext user = UserContextHolder.require();
        Position position = findById(id);
        checkDepartmentOwnership(position, user);
        if (position.getStatus() == PositionStatus.PUBLISHED) {
            throw new BusinessException(400, "已发布的岗位不可直接编辑，请先关闭");
        }
        if (position.getStatus() == PositionStatus.PENDING) {
            throw new BusinessException(400, "审批中的岗位不可编辑，请等待审批结果");
        }
        position.setTitle(request.getTitle().trim());
        position.setDescription(request.getDescription().trim());
        applyOptionalFields(position, request);
        Position saved = positionRepository.save(position);
        return PositionMapper.toResponse(saved);
    }

    @Transactional
    public void deletePosition(Long id) {
        UserContext user = UserContextHolder.require();
        Position position = findById(id);
        checkDepartmentOwnership(position, user);
        if (position.getStatus() == PositionStatus.PUBLISHED) {
            throw new BusinessException(400, "已发布的岗位不可直接删除，请联系管理员关闭");
        }
        if (position.getStatus() == PositionStatus.PENDING) {
            throw new BusinessException(400, "审批中的岗位不可删除");
        }
        positionRepository.delete(position);
    }

    @Transactional
    public PositionResponse submitForApproval(Long id) {
        UserContext user = UserContextHolder.require();
        Position position = findById(id);
        checkDepartmentOwnership(position, user);
        if (position.getStatus() != PositionStatus.DRAFT) {
            throw new BusinessException(400, "只有草稿状态的岗位可以提交审批");
        }
        position.setStatus(PositionStatus.PENDING);
        return PositionMapper.toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse approvePosition(Long id, ApprovalRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅招聘管理员可审批岗位");
        }
        Position position = findById(id);
        if (position.getStatus() != PositionStatus.PENDING) {
            throw new BusinessException(400, "只有待审批状态的岗位可以审批");
        }
        position.setStatus(PositionStatus.PUBLISHED);
        position.setApprover(user.getDisplayName());
        position.setApprovalComment(request.getApprovalComment());
        position.setPublishedAt(LocalDateTime.now());
        complianceService.log("APPROVE_POSITION", "Position", id, "审批发布");
        return PositionMapper.toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse rejectPosition(Long id, ApprovalRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅招聘管理员可驳回岗位");
        }
        Position position = findById(id);
        if (position.getStatus() != PositionStatus.PENDING) {
            throw new BusinessException(400, "只有待审批状态的岗位可以驳回");
        }
        position.setStatus(PositionStatus.DRAFT);
        position.setApprover(user.getDisplayName());
        position.setApprovalComment(request.getApprovalComment());
        return PositionMapper.toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse closePosition(Long id) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅招聘管理员可关闭岗位");
        }
        Position position = findById(id);
        if (position.getStatus() != PositionStatus.PUBLISHED) {
            throw new BusinessException(400, "只有已发布状态的岗位可以关闭");
        }
        position.setStatus(PositionStatus.CLOSED);
        return PositionMapper.toResponse(positionRepository.save(position));
    }

    public StatisticsResponse getStatistics() {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅招聘管理员可查看统计");
        }
        long total = positionRepository.count();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (PositionStatus status : PositionStatus.values()) {
            byStatus.put(status.name(), positionRepository.countByStatus(status));
        }
        return new StatisticsResponse(total, byStatus);
    }

    @Transactional
    public ImportResult importFromExcel(MultipartFile file) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.DEPARTMENT) {
            throw new BusinessException(403, "仅部门账号可导入岗位");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传 Excel 文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式");
        }

        List<String> errors = new ArrayList<>();
        List<PositionResponse> imported = new ArrayList<>();
        int total = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException(400, "Excel 文件为空");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(400, "Excel 缺少表头行");
            }

            int titleCol = findColumnIndex(headerRow, "岗位名称");
            int descCol = findColumnIndex(headerRow, "岗位描述");
            if (titleCol < 0 || descCol < 0) {
                throw new BusinessException(400, "Excel 模板必须包含「岗位名称」和「岗位描述」两列");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                total++;
                String title = getCellString(row.getCell(titleCol));
                String description = getCellString(row.getCell(descCol));
                if (title.isBlank()) {
                    errors.add("第 " + (i + 1) + " 行：岗位名称不能为空");
                    continue;
                }
                if (description.isBlank()) {
                    errors.add("第 " + (i + 1) + " 行：岗位描述不能为空");
                    continue;
                }
                Position position = new Position();
                position.setTitle(title.trim());
                position.setDescription(description.trim());
                position.setStatus(PositionStatus.DRAFT);
                position.setCreatedById(user.getUserId());
                position.setCreatedByName(user.getDisplayName());
                position.setDepartment(user.getDepartment());
                Position saved = positionRepository.save(position);
                imported.add(PositionMapper.toResponse(saved));
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(500, "解析 Excel 失败: " + ex.getMessage());
        }

        return new ImportResult(total, imported.size(), total - imported.size(), errors, imported);
    }

    public byte[] generateTemplate() {
        UserContextHolder.require();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("岗位导入模板");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("岗位名称");
            header.createCell(1).setCellValue("岗位描述");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("前端开发实习生");
            row1.createCell(1).setCellValue("岗位职责：负责页面开发；任职要求：熟悉 Vue；加分项：有实习经验");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Java后端开发实习生");
            row2.createCell(1).setCellValue("岗位职责：参与后端开发；任职要求：熟悉 Java；加分项：了解 Spring Boot");

            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 12000);

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException(500, "生成模板失败: " + ex.getMessage());
        }
    }

    private List<Position> queryAllPositions(String title, PositionStatus status) {
        if (title != null && !title.isBlank() && status != null) {
            return positionRepository.findByTitleContainingIgnoreCaseAndStatus(title.trim(), status);
        }
        if (title != null && !title.isBlank()) {
            return positionRepository.findByTitleContainingIgnoreCase(title.trim());
        }
        if (status != null) {
            return positionRepository.findByStatus(status);
        }
        return positionRepository.findAll();
    }

    private List<Position> queryDepartmentPositions(Long userId, String title, PositionStatus status) {
        if (title != null && !title.isBlank() && status != null) {
            return positionRepository.findByCreatedByIdAndTitleContainingIgnoreCaseAndStatus(userId, title.trim(), status);
        }
        if (title != null && !title.isBlank()) {
            return positionRepository.findByCreatedByIdAndTitleContainingIgnoreCase(userId, title.trim());
        }
        if (status != null) {
            return positionRepository.findByCreatedByIdAndStatus(userId, status);
        }
        return positionRepository.findByCreatedById(userId);
    }

    private void checkViewPermission(Position position) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!Objects.equals(position.getCreatedById(), user.getUserId())) {
            throw new BusinessException(403, "无权查看该岗位");
        }
    }

    private void checkDepartmentOwnership(Position position, UserContext user) {
        if (user.getRole() != UserRole.DEPARTMENT) {
            throw new BusinessException(403, "仅部门账号可操作自己的岗位");
        }
        if (!Objects.equals(position.getCreatedById(), user.getUserId())) {
            throw new BusinessException(403, "只能操作本部门提交的岗位");
        }
    }

    private Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在，ID=" + id));
    }

    private int findColumnIndex(Row headerRow, String columnName) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && columnName.equals(getCellString(cell).trim())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    public JdCopilotResponse generateJd(JdCopilotRequest request) {
        UserContext user = UserContextHolder.require();
        if (user.getRole() != UserRole.DEPARTMENT && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "无权使用 JD Copilot");
        }
        if (request.getBrief() == null || request.getBrief().isBlank()) {
            throw new BusinessException(400, "请描述岗位需求");
        }
        AiMatchingService.JdCopilotResult result = aiMatchingService.generateJd(request.getBrief().trim());
        JdCopilotResponse res = new JdCopilotResponse();
        res.setTitle(result.title());
        res.setDescription(result.description());
        res.setSkillTags(result.skillTags());
        res.setPositionType(result.positionType());
        complianceService.log("JD_COPILOT", "System", null, request.getBrief());
        return res;
    }

    public String remindApproval(Long positionId) {
        UserContext user = UserContextHolder.require();
        Position position = findById(positionId);
        checkDepartmentOwnership(position, user);
        if (position.getStatus() != PositionStatus.PENDING) {
            throw new BusinessException(400, "仅审批中的岗位可催办");
        }
        complianceService.log("REMIND_APPROVAL", "Position", positionId,
                user.getDisplayName() + " 催办岗位审批：" + position.getTitle());
        return "已向 HR 发送催办提醒（" + position.getTitle() + "）";
    }

    public String shareCandidate(Long positionId, Long applicationId, String targetUser) {
        UserContext user = UserContextHolder.require();
        Position position = findById(positionId);
        checkDepartmentOwnership(position, user);
        complianceService.log("SHARE_CANDIDATE", "Application", applicationId,
                "分享给 " + (targetUser != null ? targetUser : "团队成员") + "，岗位：" + position.getTitle());
        return "已将候选人档案分享至 " + (targetUser != null ? targetUser : "团队群组");
    }

    private void applyOptionalFields(Position position, PositionRequest request) {
        if (request.getPositionType() != null && !request.getPositionType().isBlank()) {
            position.setPositionType(request.getPositionType().trim());
        }
        if (request.getSkillTags() != null) {
            position.setSkillTags(request.getSkillTags().trim());
        }
        if (position.getWorkflowSteps() == null || position.getWorkflowSteps().isBlank()) {
            try {
                position.setWorkflowSteps(new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(workflowService.getWorkflow(position)));
            } catch (Exception ignored) {
            }
        }
    }
}
