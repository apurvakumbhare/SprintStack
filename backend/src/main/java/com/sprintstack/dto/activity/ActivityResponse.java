package com.sprintstack.dto.activity;
import com.sprintstack.entity.ActivityLog;
import java.time.LocalDateTime;
import java.util.Base64;
public record ActivityResponse(Long id,String userName,Long projectId,String projectName,String actionType,String entityType,Long entityId,String description,LocalDateTime createdAt,String profilePhoto){
 public static ActivityResponse from(ActivityLog a){byte[] photo=a.getUser().getProfilePhoto();String profilePhoto=photo==null?null:"data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(photo);return new ActivityResponse(a.getId(),a.getUser().getFullName(),a.getProject()==null?null:a.getProject().getId(),a.getProject()==null?null:a.getProject().getName(),a.getActionType(),a.getRelatedEntityType(),a.getRelatedEntityId(),a.getDescription(),a.getCreatedAt(),profilePhoto);}
}
