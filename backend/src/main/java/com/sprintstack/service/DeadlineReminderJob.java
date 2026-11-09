package com.sprintstack.service;

import com.sprintstack.entity.Task;
import com.sprintstack.entity.TaskStatus;
import com.sprintstack.repository.NotificationRepository;
import com.sprintstack.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class DeadlineReminderJob {
    private final TaskRepository tasks;
    private final NotificationRepository notifications;
    private final WorkspaceEventService events;
    private final ZoneId zone;

    public DeadlineReminderJob(TaskRepository tasks,NotificationRepository notifications,WorkspaceEventService events,
                               @Value("${app.timezone:Asia/Calcutta}") String timezone){
        this.tasks=tasks;this.notifications=notifications;this.events=events;this.zone=ZoneId.of(timezone);
    }

    @Scheduled(cron="0 0 9 * * *",zone="${app.timezone:Asia/Calcutta}")
    public void sendDeadlineReminders(){
        LocalDate today=LocalDate.now(zone);
        LocalDateTime since=today.atStartOfDay();
        for(Task task:tasks.findAllByDueDateAndAssigneeIsNotNullAndStatusNot(today.plusDays(1),TaskStatus.COMPLETED)){
            notifyOnce(task,"TASK_DUE_SOON","Task “"+task.getTitle()+"” is due tomorrow",since);
        }
        for(Task task:tasks.findAllByDueDateBeforeAndAssigneeIsNotNullAndStatusNot(today,TaskStatus.COMPLETED)){
            notifyOnce(task,"TASK_OVERDUE","Task “"+task.getTitle()+"” is overdue",since);
        }
    }

    private void notifyOnce(Task task,String type,String message,LocalDateTime since){
        Long userId=task.getAssignee().getId();
        if(!notifications.existsByUser_IdAndTypeAndRelatedEntityIdAndCreatedAtAfter(userId,type,task.getId(),since))
            events.notify(task.getAssignee(),type,message,"TASK",task.getId());
    }
}
