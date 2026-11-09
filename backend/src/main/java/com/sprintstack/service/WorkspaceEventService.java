package com.sprintstack.service;

import com.sprintstack.entity.*;
import com.sprintstack.repository.ActivityLogRepository;
import com.sprintstack.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceEventService {
    private final ActivityLogRepository activity;
    private final NotificationRepository notifications;
    public WorkspaceEventService(ActivityLogRepository activity,NotificationRepository notifications){this.activity=activity;this.notifications=notifications;}
    public void record(User actor,Project project,String action,String entityType,Long entityId,String description){
        activity.save(new ActivityLog(actor,project,action,entityType,entityId,description));
    }
    public void notify(User recipient,String type,String message,String entityType,Long entityId){
        if(recipient!=null)notifications.save(new Notification(recipient,type,message,entityType,entityId));
    }
}
