package com.sprintstack.dto.project;

import com.sprintstack.entity.Project;
import com.sprintstack.entity.ProjectMember;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Base64;

public record ProjectResponse(Long id, String name, String description, String status, String priority,
                              LocalDate startDate, LocalDate dueDate, Long createdById,
                              String createdByName, int memberCount, List<MemberSummary> members,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
    public record MemberSummary(Long id, String fullName, String email, String role, String profilePhoto) { }
    public static ProjectResponse from(Project project) {
        List<MemberSummary> members = project.getMembers().stream()
                .map(member -> new MemberSummary(member.getUser().getId(), member.getUser().getFullName(),
                        member.getUser().getEmail(), member.getRole().name(), photoData(member.getUser().getProfilePhoto())))
                .toList();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getStatus().name(),
                project.getPriority().name(), project.getStartDate(), project.getDueDate(), project.getCreatedBy().getId(),
                project.getCreatedBy().getFullName(), members.size(), members, project.getCreatedAt(), project.getUpdatedAt());
    }

    private static String photoData(byte[] photo) {
        return photo == null ? null : "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photo);
    }
}
