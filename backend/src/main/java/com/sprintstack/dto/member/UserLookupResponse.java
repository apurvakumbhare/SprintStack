package com.sprintstack.dto.member;

import com.sprintstack.entity.User;

public record UserLookupResponse(Long id, String fullName, String email) {
    public static UserLookupResponse from(User user){return new UserLookupResponse(user.getId(),user.getFullName(),user.getEmail());}
}
