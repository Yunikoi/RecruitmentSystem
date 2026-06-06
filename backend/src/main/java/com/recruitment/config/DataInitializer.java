package com.recruitment.config;

import com.recruitment.entity.*;
import com.recruitment.repository.*;
import com.recruitment.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               PositionRepository positionRepository,
                               ApplicationRepository applicationRepository,
                               InterviewRepository interviewRepository,
                               InterviewSlotRepository interviewSlotRepository,
                               AuthService authService) {
        return args -> {
            User admin = createUser(userRepository, authService, "admin", "admin123", "招聘HR", "人事部", UserRole.ADMIN);
            User deptHr = createUser(userRepository, authService, "dept_hr", "dept123", "人事部专员", "人事部", UserRole.DEPARTMENT);
            User deptTech = createUser(userRepository, authService, "dept_tech", "dept123", "技术部专员", "技术部", UserRole.DEPARTMENT);
            User candidate = createUser(userRepository, authService, "candidate", "candidate123", "张同学", "求职者", UserRole.CANDIDATE);
            User interviewer = createUser(userRepository, authService, "interviewer", "interview123", "李面试官", "技术部", UserRole.INTERVIEWER);
            User executive = createUser(userRepository, authService, "executive", "exec123", "王总监", "管理层", UserRole.EXECUTIVE);

            Map<String, User> deptUsers = Map.of(
                    "技术部", deptTech,
                    "人事部", deptHr,
                    "产品部", deptTech,
                    "运营部", deptHr,
                    "市场部", deptHr,
                    "财务部", deptHr
            );

            boolean freshDb = positionRepository.count() == 0;
            List<Position> positions = seedPositions(positionRepository, admin, deptUsers);

            if (freshDb && !positions.isEmpty()) {
                seedDemoApplications(applicationRepository, interviewRepository, interviewSlotRepository,
                        candidate, interviewer, admin, positions);
            }
        };
    }

    private List<Position> seedPositions(PositionRepository positionRepository, User admin,
                                         Map<String, User> deptUsers) {
        Map<String, Position> existingByTitle = positionRepository.findAll().stream()
                .collect(Collectors.toMap(Position::getTitle, Function.identity(), (a, b) -> a));

        List<Position> result = new ArrayList<>();
        for (DemoPositionSeeds.Seed seed : DemoPositionSeeds.all()) {
            if (existingByTitle.containsKey(seed.title())) {
                result.add(existingByTitle.get(seed.title()));
                continue;
            }
            User creator = deptUsers.getOrDefault(seed.department(), admin);
            User approver = seed.status() == PositionStatus.PUBLISHED ? admin : null;
            Position p = createPosition(seed.title(), seed.description(), seed.status(), creator, approver);
            p.setDepartment(seed.department());
            p.setPositionType(seed.positionType());
            p.setSkillTags(seed.skillTags());
            Position saved = positionRepository.save(p);
            existingByTitle.put(seed.title(), saved);
            result.add(saved);
        }

        return result;
    }

    private void seedDemoApplications(ApplicationRepository applicationRepository,
                                      InterviewRepository interviewRepository,
                                      InterviewSlotRepository interviewSlotRepository,
                                      User candidate, User interviewer, User admin,
                                      List<Position> positions) {
        Position javaPos = findByTitle(positions, "Java后端开发工程师");
        Position fePos = findByTitle(positions, "前端开发工程师");
        if (javaPos == null || fePos == null) {
            return;
        }

        Application demo = new Application();
        demo.setPositionId(javaPos.getId());
        demo.setCandidateId(candidate.getId());
        demo.setCandidateName("张同学");
        demo.setCandidateEmail("zhang@example.com");
        demo.setCandidatePhone("13800138000");
        demo.setStage(ApplicationStage.BUSINESS_INTERVIEW);
        demo.setChannel(ChannelType.OFFICIAL);
        demo.setResumeText("Java Spring Boot 微服务 3年经验");
        demo.setParsedSkills("Java、Spring Boot、MySQL");
        demo.setMatchScore(88);
        demo.setMatchHighlights("核心匹配：Java、Spring、微服务");
        demo.setMatchRisks("");
        demo.setAiInterviewScore(86);
        demo.setAiInterviewPass(true);
        demo.setAiInterviewFeedback("表达清晰，Java/Spring 基础扎实，项目描述完整，建议进入业务面进一步考察。");
        demo.setAiInterviewAt(LocalDateTime.now().minusDays(1));
        demo.setStageUpdatedAt(LocalDateTime.now());
        applicationRepository.save(demo);

        Application demo2 = new Application();
        demo2.setPositionId(fePos.getId());
        demo2.setCandidateId(candidate.getId());
        demo2.setCandidateName("张同学");
        demo2.setCandidateEmail("zhang@example.com");
        demo2.setCandidatePhone("13800138000");
        demo2.setStage(ApplicationStage.SCREENING);
        demo2.setChannel(ChannelType.OFFICIAL);
        demo2.setResumeText("Vue React TypeScript 2年经验");
        demo2.setParsedSkills("Vue、React、TypeScript");
        demo2.setMatchScore(82);
        demo2.setMatchHighlights("熟悉 Vue 生态");
        demo2.setMatchRisks("React 经验略少");
        demo2.setStageUpdatedAt(LocalDateTime.now());
        applicationRepository.save(demo2);

        // 前端岗：未完成 AI 初试，不预置业务面邀请（须候选人先完成 AI 语音初试）

        Interview scheduled = new Interview();
        scheduled.setApplicationId(demo.getId());
        scheduled.setInterviewerId(interviewer.getId());
        scheduled.setInterviewerName(interviewer.getDisplayName());
        scheduled.setType("BUSINESS");
        scheduled.setScheduledAt(LocalDateTime.now().plusDays(2));
        scheduled.setLocation("线上 · 腾讯会议 https://meeting.tencent.com/demo");
        scheduled.setStatus("PENDING");
        scheduled.setInvitedById(admin.getId());
        scheduled.setInvitedByName(admin.getDisplayName());
        scheduled.setExpireAt(LocalDateTime.now().plusDays(3));
        interviewRepository.save(scheduled);

        InterviewSlot slot1 = new InterviewSlot();
        slot1.setInterviewerId(interviewer.getId());
        slot1.setInterviewerName(interviewer.getDisplayName());
        slot1.setPositionId(javaPos.getId());
        slot1.setStartTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        slot1.setEndTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0));
        slot1.setBooked(false);
        InterviewSlot slot2 = new InterviewSlot();
        slot2.setInterviewerId(interviewer.getId());
        slot2.setInterviewerName(interviewer.getDisplayName());
        slot2.setPositionId(javaPos.getId());
        slot2.setStartTime(LocalDateTime.now().plusDays(4).withHour(14).withMinute(0));
        slot2.setEndTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0));
        slot2.setBooked(false);
        interviewSlotRepository.save(slot1);
        interviewSlotRepository.save(slot2);

        InterviewSlot slot3 = new InterviewSlot();
        slot3.setInterviewerId(interviewer.getId());
        slot3.setInterviewerName(interviewer.getDisplayName());
        slot3.setPositionId(fePos.getId());
        slot3.setStartTime(LocalDateTime.now().plusDays(6).withHour(10).withMinute(0));
        slot3.setEndTime(LocalDateTime.now().plusDays(6).withHour(11).withMinute(0));
        slot3.setBooked(false);
        interviewSlotRepository.save(slot3);
    }

    private Position findByTitle(List<Position> positions, String title) {
        return positions.stream()
                .filter(p -> title.equals(p.getTitle()))
                .findFirst()
                .orElse(null);
    }

    private User createUser(UserRepository repo, AuthService auth, String username, String password,
                            String displayName, String department, UserRole role) {
        return repo.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword(auth.encodePassword(password));
            u.setDisplayName(displayName);
            u.setDepartment(department);
            u.setRole(role);
            return repo.save(u);
        });
    }

    private Position createPosition(String title, String desc, PositionStatus status, User creator, User approver) {
        Position p = new Position();
        p.setTitle(title);
        p.setDescription(desc);
        p.setStatus(status);
        p.setCreatedById(creator.getId());
        p.setCreatedByName(creator.getDisplayName());
        p.setDepartment(creator.getDepartment());
        if (approver != null) {
            p.setApprover(approver.getDisplayName());
            p.setApprovalComment("符合互联网人才标准");
            p.setPublishedAt(LocalDateTime.now());
        }
        return p;
    }
}
