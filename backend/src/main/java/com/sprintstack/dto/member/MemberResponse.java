package com.sprintstack.dto.member;

import com.sprintstack.entity.ProjectMember;
import java.time.LocalDateTime;
import java.util.Base64;

public record MemberResponse(Long userId, String fullName, String email, String role, LocalDateTime joinedAt, String profilePhoto) {
    public static MemberResponse from(ProjectMember member) {
        byte[] photo = member.getUser().getProfilePhoto();
        String profilePhoto = photo == null ? null : "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photo);
        return new MemberResponse(member.getUser().getId(),member.getUser().getFullName(),member.getUser().getEmail(),member.getRole().name(),member.getJoinedAt(),profilePhoto);
    }
}
