package com.sprintstack.controller;

import com.sprintstack.dto.analytics.ProjectAnalyticsResponse;
import com.sprintstack.service.ProjectAnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/analytics")
public class ProjectAnalyticsController {
    private final ProjectAnalyticsService analytics;

    public ProjectAnalyticsController(ProjectAnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping
    public ProjectAnalyticsResponse summary(Authentication authentication, @PathVariable Long projectId) {
        return analytics.summary(authentication.getName(), projectId);
    }
}
