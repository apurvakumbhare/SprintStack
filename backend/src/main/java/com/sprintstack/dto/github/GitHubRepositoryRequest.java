package com.sprintstack.dto.github;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GitHubRepositoryRequest(@NotBlank @Size(max = 300) String repositoryUrl) { }
