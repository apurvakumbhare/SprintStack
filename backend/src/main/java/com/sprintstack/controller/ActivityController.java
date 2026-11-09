package com.sprintstack.controller;

import com.sprintstack.dto.activity.ActivityResponse;
import com.sprintstack.repository.ActivityLogRepository;
import com.sprintstack.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
public class ActivityController {
    private final ProjectService projects;
    private final ActivityLogRepository activity;
    public ActivityController(ProjectService projects,ActivityLogRepository activity){this.projects=projects;this.activity=activity;}
    @GetMapping public List<ActivityResponse> list(Authentication auth,@PathVariable Long projectId){
        projects.projectForMember(auth.getName(),projectId);
        return activity.findTop50ByProject_IdOrderByCreatedAtDesc(projectId).stream().map(ActivityResponse::from).toList();
    }
}
