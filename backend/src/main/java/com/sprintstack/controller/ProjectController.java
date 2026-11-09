package com.sprintstack.controller;

import com.sprintstack.dto.project.ProjectRequest;
import com.sprintstack.dto.project.ProjectResponse;
import com.sprintstack.dto.project.ProjectUpdateRequest;
import com.sprintstack.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;
    public ProjectController(ProjectService projects) { this.projects=projects; }

    @GetMapping
    public List<ProjectResponse> list(Authentication authentication) { return projects.list(authentication.getName()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(Authentication authentication, @Valid @RequestBody ProjectRequest request) {
        return projects.create(authentication.getName(), request);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(Authentication authentication, @PathVariable Long projectId) {
        return projects.get(authentication.getName(), projectId);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse update(Authentication authentication, @PathVariable Long projectId,
                                  @Valid @RequestBody ProjectUpdateRequest request) {
        return projects.update(authentication.getName(), projectId, request);
    }

    @PatchMapping("/{projectId}/archive")
    public ProjectResponse archive(Authentication authentication, @PathVariable Long projectId) {
        return projects.archive(authentication.getName(), projectId);
    }
}
