package com.sprintstack.dto.project;

import com.sprintstack.entity.Project;
import com.sprintstack.entity.ProjectMember;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(Long id, String name, String description, String status, String priority,
                              LocalDate startDate, LocalDate dueDate, Long createdById,
                              String createdByName, int memberCount, List<MemberSummary> members,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
    public record MemberSummary(Long id, String fullName, String email, String role) { }
    public static ProjectResponse from(Project project) {
        List<MemberSummary> members = project.getMembers().stream()
                .map(member -> new MemberSummary(member.getUser().getId(), member.getUser().getFullName(),
                        member.getUser().getEmail(), member.getRole().name()))
                .toList();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getStatus().name(),
                project.getPriority().name(), project.getStartDate(), project.getDueDate(), project.getCreatedBy().getId(),
                project.getCreatedBy().getFullName(), members.size(), members, project.getCreatedAt(), project.getUpdatedAt());
    }
}
