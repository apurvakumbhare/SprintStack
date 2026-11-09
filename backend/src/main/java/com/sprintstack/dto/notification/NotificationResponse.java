package com.sprintstack.dto.notification;
import com.sprintstack.entity.Notification;
import java.time.LocalDateTime;
public record NotificationResponse(Long id,String type,String message,String entityType,Long entityId,boolean read,LocalDateTime createdAt){
 public static NotificationResponse from(Notification n){return new NotificationResponse(n.getId(),n.getType(),n.getMessage(),n.getRelatedEntityType(),n.getRelatedEntityId(),n.isRead(),n.getCreatedAt());}
}
