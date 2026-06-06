package com.recruitment.service;

import com.recruitment.context.UserContextHolder;
import com.recruitment.dto.DuplicateGroupResponse;
import com.recruitment.entity.Application;
import com.recruitment.entity.UserRole;
import com.recruitment.exception.BusinessException;
import com.recruitment.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateMergeService {

    private final ApplicationRepository applicationRepository;
    private final ComplianceService complianceService;

    public DuplicateMergeService(ApplicationRepository applicationRepository,
                                 ComplianceService complianceService) {
        this.applicationRepository = applicationRepository;
        this.complianceService = complianceService;
    }

    public List<DuplicateGroupResponse> findDuplicates() {
        UserContextHolder.require();
        List<Application> all = applicationRepository.findAll().stream()
                .filter(a -> a.getMergedIntoId() == null)
                .collect(Collectors.toList());
        Map<String, List<Application>> byEmail = all.stream()
                .filter(a -> a.getCandidateEmail() != null && !a.getCandidateEmail().isBlank())
                .collect(Collectors.groupingBy(a -> a.getCandidateEmail().toLowerCase()));
        Map<String, List<Application>> byPhone = all.stream()
                .filter(a -> a.getCandidatePhone() != null && !a.getCandidatePhone().isBlank())
                .collect(Collectors.groupingBy(Application::getCandidatePhone));

        Set<Long> seen = new HashSet<>();
        List<DuplicateGroupResponse> groups = new ArrayList<>();

        for (List<Application> list : byEmail.values()) {
            if (list.size() > 1) addGroup(groups, seen, list, "EMAIL");
        }
        for (List<Application> list : byPhone.values()) {
            if (list.size() > 1) addGroup(groups, seen, list, "PHONE");
        }
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Application a = all.get(i), b = all.get(j);
                if (semanticSimilar(a, b) && !seen.contains(b.getId())) {
                    addGroup(groups, seen, List.of(a, b), "SEMANTIC");
                }
            }
        }
        return groups;
    }

    @Transactional
    public void merge(Long primaryId, List<Long> mergeIds) {
        var user = UserContextHolder.require();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅HR可合并档案");
        }
        Application primary = applicationRepository.findById(primaryId)
                .orElseThrow(() -> new BusinessException(404, "主档案不存在"));
        for (Long id : mergeIds) {
            if (id.equals(primaryId)) continue;
            Application dup = applicationRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "档案不存在: " + id));
            dup.setMergedIntoId(primaryId);
            applicationRepository.save(dup);
        }
        complianceService.log("MERGE_PROFILE", "Application", primaryId,
                "合并档案: " + mergeIds);
    }

    private void addGroup(List<DuplicateGroupResponse> groups, Set<Long> seen,
                          List<Application> list, String reason) {
        List<Long> ids = list.stream().map(Application::getId).filter(seen::add).toList();
        if (ids.size() < 2) return;
        DuplicateGroupResponse g = new DuplicateGroupResponse();
        g.setReason(reason);
        g.setApplicationIds(ids);
        g.setCandidateNames(list.stream().map(Application::getCandidateName).distinct().collect(Collectors.toList()));
        g.setEmails(list.stream().map(Application::getCandidateEmail).distinct().collect(Collectors.toList()));
        groups.add(g);
    }

    private boolean semanticSimilar(Application a, Application b) {
        if (a.getCandidateName() != null && a.getCandidateName().equals(b.getCandidateName())) {
            String s1 = a.getParsedSkills() != null ? a.getParsedSkills() : "";
            String s2 = b.getParsedSkills() != null ? b.getParsedSkills() : "";
            return !s1.isBlank() && s1.equals(s2);
        }
        return false;
    }
}
