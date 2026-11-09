package com.sprintstack.dto.activity;
import com.sprintstack.entity.ActivityLog;
import java.time.LocalDateTime;
public record ActivityResponse(Long id,String userName,Long projectId,String projectName,String actionType,String entityType,Long entityId,String description,LocalDateTime createdAt){
 public static ActivityResponse from(ActivityLog a){return new ActivityResponse(a.getId(),a.getUser().getFullName(),a.getProject()==null?null:a.getProject().getId(),a.getProject()==null?null:a.getProject().getName(),a.getActionType(),a.getRelatedEntityType(),a.getRelatedEntityId(),a.getDescription(),a.getCreatedAt());}
}
