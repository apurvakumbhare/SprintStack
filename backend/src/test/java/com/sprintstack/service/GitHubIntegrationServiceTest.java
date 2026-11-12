package com.sprintstack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintstack.dto.github.GitHubRepositoryRequest;
import com.sprintstack.entity.Project;
import com.sprintstack.entity.ProjectMember;
import com.sprintstack.entity.ProjectRole;
import com.sprintstack.entity.User;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubIntegrationServiceTest {
    @Test
    void onlyOwnerCanLinkRepository() {
        ProjectService projects = mock(ProjectService.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ProjectMemberRepository members = mock(ProjectMemberRepository.class);
        User actor = mock(User.class);
        when(actor.getId()).thenReturn(8L);
        when(projects.userByEmail("dev@example.com")).thenReturn(actor);
        when(members.findByProject_IdAndUser_Id(4L, 8L)).thenReturn(Optional.of(new ProjectMember(null, actor, ProjectRole.DEVELOPER)));
        GitHubIntegrationService service = new GitHubIntegrationService(projects, projectRepository, members, new ObjectMapper());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.setRepository("dev@example.com", 4L, new GitHubRepositoryRequest("https://github.com/acme/app")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void ownerCanSaveCanonicalRepositoryUrl() {
        ProjectService projects = mock(ProjectService.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ProjectMemberRepository members = mock(ProjectMemberRepository.class);
        User actor = mock(User.class);
        when(actor.getId()).thenReturn(8L);
        when(projects.userByEmail("owner@example.com")).thenReturn(actor);
        when(members.findByProject_IdAndUser_Id(4L, 8L)).thenReturn(Optional.of(new ProjectMember(null, actor, ProjectRole.OWNER)));
        Project project = new Project("Work", null, null, null, null, null, actor);
        when(projectRepository.findById(4L)).thenReturn(Optional.of(project));
        GitHubIntegrationService service = new GitHubIntegrationService(projects, projectRepository, members, new ObjectMapper());

        String result = service.setRepository("owner@example.com", 4L, new GitHubRepositoryRequest("https://github.com/acme/app.git/"));

        assertEquals("https://github.com/acme/app", result);
        assertEquals(result, project.getGithubRepoUrl());
    }

    @Test
    void rejectsNonGitHubRepositoryUrls() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> GitHubIntegrationService.canonicalRepositoryUrl("https://example.com/acme/app"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }
}
