package com.sprintstack.dto.member;

import com.sprintstack.entity.ProjectMember;
import java.time.LocalDateTime;

public record MemberResponse(Long userId, String fullName, String email, String role, LocalDateTime joinedAt) {
    public static MemberResponse from(ProjectMember member) {
        return new MemberResponse(member.getUser().getId(),member.getUser().getFullName(),member.getUser().getEmail(),member.getRole().name(),member.getJoinedAt());
    }
}
