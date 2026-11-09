package com.sprintstack.controller;

import com.sprintstack.dto.DashboardResponse;
import com.sprintstack.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard=dashboard; }
    @GetMapping
    public DashboardResponse summary(Authentication authentication) { return dashboard.summary(authentication.getName()); }
}
