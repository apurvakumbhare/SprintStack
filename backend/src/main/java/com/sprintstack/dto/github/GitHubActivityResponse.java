package com.sprintstack.dto.github;

import java.time.Instant;

public record GitHubActivityResponse(String id, String type, String title, String actor,
                                     String actorAvatar, String htmlUrl, String ref,
                                     Instant createdAt) { }
