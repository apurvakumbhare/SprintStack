package com.sprintstack.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfilePhotoRequest(@NotBlank @Size(max = 1_500_000) String photo) { }
