package com.sprintstack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintstack.dto.github.GitHubActivityResponse;
import com.sprintstack.dto.github.GitHubRepositoryRequest;
import com.sprintstack.entity.Project;
import com.sprintstack.entity.ProjectMember;
import com.sprintstack.entity.ProjectRole;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class GitHubIntegrationService {
    private static final Pattern REPOSITORY_PATH = Pattern.compile("^/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+?)(?:\\.git)?/?$");
    private final ProjectService projects;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository members;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final Map<String, CachedActivity> activityCache = new ConcurrentHashMap<>();

    public GitHubIntegrationService(ProjectService projects, ProjectRepository projectRepository,
                                    ProjectMemberRepository members, ObjectMapper objectMapper) {
        this.projects = projects;
        this.projectRepository = projectRepository;
        this.members = members;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String setRepository(String email, Long projectId, GitHubRepositoryRequest request) {
        var actor = projects.userByEmail(email);
        ProjectMember membership = members.findByProject_IdAndUser_Id(projectId, actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
        if (membership.getRole() != ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can link a GitHub repository");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        String canonicalUrl = canonicalRepositoryUrl(request.repositoryUrl());
        project.updateGithubRepoUrl(canonicalUrl);
        return canonicalUrl;
    }

    @Transactional
    public void removeRepository(String email, Long projectId) {
        var actor = projects.userByEmail(email);
        ProjectMember membership = members.findByProject_IdAndUser_Id(projectId, actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
        if (membership.getRole() != ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can remove the GitHub repository");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.updateGithubRepoUrl(null);
    }

    @Transactional(readOnly = true)
    public List<GitHubActivityResponse> recentActivity(String email, Long projectId) {
        Project project = projects.projectForMember(email, projectId);
        if (project.getGithubRepoUrl() == null) return List.of();
        String[] path = canonicalRepositoryPath(project.getGithubRepoUrl());
        String repository = path[0] + "/" + path[1];
        CachedActivity cached = activityCache.get(repository);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) return cached.items();
        try {
            JsonNode commitData = fetchJson("https://api.github.com/repos/" + repository + "/commits?per_page=10");
            JsonNode pullData = fetchJson("https://api.github.com/repos/" + repository + "/pulls?state=all&sort=updated&direction=desc&per_page=10");
            List<GitHubActivityResponse> items = toActivity(commitData, pullData);
            activityCache.put(repository, new CachedActivity(Instant.now().plusSeconds(300), items));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "The GitHub request was interrupted.");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not read activity from GitHub.");
        }
    }

    static String canonicalRepositoryUrl(String rawUrl) {
        String[] parts = canonicalRepositoryPath(rawUrl);
        return "https://github.com/" + parts[0] + "/" + parts[1];
    }

    private static String[] canonicalRepositoryPath(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != -1 || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
            var matcher = REPOSITORY_PATH.matcher(uri.getPath());
            if (!matcher.matches()) throw new IllegalArgumentException();
            String owner = matcher.group(1);
            String repository = matcher.group(2);
            if (owner.equals(".") || owner.equals("..") || repository.equals(".") || repository.equals("..")) throw new IllegalArgumentException();
            if (repository.toLowerCase().endsWith(".git")) repository = repository.substring(0, repository.length() - 4);
            return new String[]{owner, repository};
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid repository link such as https://github.com/owner/repository");
        }
    }

    private JsonNode fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "Project-Activity-Integration")
                .GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub could not find this repository. Check that it is public and the link is correct.");
        if (response.statusCode() == 403 || response.statusCode() == 429) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GitHub temporarily limited requests. Try again later.");
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub could not load repository activity.");
        return objectMapper.readTree(response.body());
    }

    private List<GitHubActivityResponse> toActivity(JsonNode commits, JsonNode pulls) {
        if (!commits.isArray() || !pulls.isArray()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub returned an unexpected activity response.");
        List<GitHubActivityResponse> items = new ArrayList<>();
        for (JsonNode node : commits) {
            String sha = node.path("sha").asText("");
            if (sha.isBlank()) continue;
            String date = node.path("commit").path("author").path("date").asText("");
            items.add(new GitHubActivityResponse("commit-" + sha, "Commit",
                    node.path("commit").path("message").asText("Commit").split("\\R", 2)[0],
                    node.path("author").path("login").asText(node.path("commit").path("author").path("name").asText("Contributor")),
                    node.path("author").path("avatar_url").asText(null), node.path("html_url").asText(""), "",
                    parseInstant(date)));
        }
        for (JsonNode node : pulls) {
            String number = node.path("number").asText("");
            if (number.isBlank()) continue;
            items.add(new GitHubActivityResponse("pull-" + number, "Pull request",
                    node.path("title").asText("Pull request") + " · " + node.path("state").asText("updated"),
                    node.path("user").path("login").asText("Contributor"), node.path("user").path("avatar_url").asText(null),
                    node.path("html_url").asText(""), "#" + number, parseInstant(node.path("updated_at").asText(""))));
        }
        return items.stream().sorted((left, right) -> right.createdAt().compareTo(left.createdAt())).limit(20).toList();
    }

    private static Instant parseInstant(String value) {
        try { return value == null || value.isBlank() ? Instant.EPOCH : Instant.parse(value); }
        catch (RuntimeException ignored) { return Instant.EPOCH; }
    }

    private record CachedActivity(Instant expiresAt, List<GitHubActivityResponse> items) { }
}
