package com.recruitment.config;

import com.recruitment.entity.*;
import com.recruitment.repository.*;
import com.recruitment.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

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

            if (positionRepository.count() > 0) return;

            Position p1 = createPosition("Java后端开发工程师", "岗位职责：负责高并发后端服务；任职要求：Java/Spring/微服务；加分项：开源贡献", PositionStatus.PUBLISHED, deptTech, admin);
            Position p2 = createPosition("前端开发工程师", "岗位职责：Web前端开发；任职要求：Vue/React/TypeScript；加分项：组件库经验", PositionStatus.PUBLISHED, deptTech, admin);
            Position p3 = createPosition("产品经理", "岗位职责：需求分析与产品设计；任职要求：互联网产品经验；加分项：数据驱动", PositionStatus.PENDING, deptHr, null);
            positionRepository.save(p1);
            positionRepository.save(p2);
            positionRepository.save(p3);

            Application demo = new Application();
            demo.setPositionId(p1.getId());
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
            demo.setStageUpdatedAt(LocalDateTime.now());
            applicationRepository.save(demo);

            // 第二条投递：前端岗 · 初筛阶段，用于演示模拟面试 + 待确认邀请 + 多 Job 隔离
            Application demo2 = new Application();
            demo2.setPositionId(p2.getId());
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

            Interview pendingInvite = new Interview();
            pendingInvite.setApplicationId(demo2.getId());
            pendingInvite.setInterviewerId(interviewer.getId());
            pendingInvite.setInterviewerName(interviewer.getDisplayName());
            pendingInvite.setType("BUSINESS");
            pendingInvite.setScheduledAt(LocalDateTime.now().plusDays(5).withHour(15).withMinute(0));
            pendingInvite.setLocation("3号会议室 / 线上腾讯会议");
            pendingInvite.setStatus("PENDING");
            pendingInvite.setInvitedById(admin.getId());
            pendingInvite.setInvitedByName(admin.getDisplayName());
            pendingInvite.setExpireAt(LocalDateTime.now().plusDays(3));
            interviewRepository.save(pendingInvite);

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
            slot1.setPositionId(p1.getId());
            slot1.setStartTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
            slot1.setEndTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0));
            slot1.setBooked(false);
            InterviewSlot slot2 = new InterviewSlot();
            slot2.setInterviewerId(interviewer.getId());
            slot2.setInterviewerName(interviewer.getDisplayName());
            slot2.setPositionId(p1.getId());
            slot2.setStartTime(LocalDateTime.now().plusDays(4).withHour(14).withMinute(0));
            slot2.setEndTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0));
            slot2.setBooked(false);
            interviewSlotRepository.save(slot1);
            interviewSlotRepository.save(slot2);

            InterviewSlot slot3 = new InterviewSlot();
            slot3.setInterviewerId(interviewer.getId());
            slot3.setInterviewerName(interviewer.getDisplayName());
            slot3.setPositionId(p2.getId());
            slot3.setStartTime(LocalDateTime.now().plusDays(6).withHour(10).withMinute(0));
            slot3.setEndTime(LocalDateTime.now().plusDays(6).withHour(11).withMinute(0));
            slot3.setBooked(false);
            interviewSlotRepository.save(slot3);

            p1.setPositionType("TECH");
            p1.setSkillTags("Java,Spring Boot,微服务");
            positionRepository.save(p1);
        };
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
