package com.sprintstack.controller;

import com.sprintstack.dto.activity.ActivityResponse;
import com.sprintstack.repository.ActivityLogRepository;
import com.sprintstack.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class WorkspaceActivityController {
    private final ProjectService projects;
    private final ActivityLogRepository activity;

    public WorkspaceActivityController(ProjectService projects, ActivityLogRepository activity) {
        this.projects = projects;
        this.activity = activity;
    }

    @GetMapping
    public List<ActivityResponse> list(Authentication authentication) {
        List<Long> projectIds = projects.list(authentication.getName()).stream()
                .map(project -> project.id()).toList();
        if (projectIds.isEmpty()) return List.of();
        return activity.findTop100ByProject_IdInOrderByCreatedAtDesc(projectIds)
                .stream().map(ActivityResponse::from).toList();
    }
}
