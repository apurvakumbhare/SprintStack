package com.sprintstack.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ProfileUpdateRequest(@NotBlank @Size(max=120) String fullName,@NotBlank @Email @Size(max=190) String email){}
