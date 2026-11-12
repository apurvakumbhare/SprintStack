package com.sprintstack.controller;

import com.sprintstack.dto.github.GitHubActivityResponse;
import com.sprintstack.dto.github.GitHubRepositoryRequest;
import com.sprintstack.service.GitHubIntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/github")
public class ProjectGitHubController {
    private final GitHubIntegrationService github;

    public ProjectGitHubController(GitHubIntegrationService github) { this.github = github; }

    @PutMapping("/repository")
    public Map<String, String> linkRepository(Authentication authentication, @PathVariable Long projectId,
                                              @Valid @RequestBody GitHubRepositoryRequest request) {
        return Map.of("repositoryUrl", github.setRepository(authentication.getName(), projectId, request));
    }

    @DeleteMapping("/repository")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRepository(Authentication authentication, @PathVariable Long projectId) {
        github.removeRepository(authentication.getName(), projectId);
    }

    @GetMapping("/commits")
    public List<GitHubActivityResponse> recentActivity(Authentication authentication, @PathVariable Long projectId) {
        return github.recentActivity(authentication.getName(), projectId);
    }
}
