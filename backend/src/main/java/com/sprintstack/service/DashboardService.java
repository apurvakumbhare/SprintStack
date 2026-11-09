package com.sprintstack.service;

import com.sprintstack.dto.DashboardResponse;
import com.sprintstack.dto.activity.ActivityResponse;
import com.sprintstack.entity.Project;
import com.sprintstack.entity.ProjectStatus;
import com.sprintstack.entity.TaskStatus;
import com.sprintstack.entity.User;
import com.sprintstack.entity.Task;
import com.sprintstack.entity.ProjectMember;
import com.sprintstack.repository.ProjectRepository;
import com.sprintstack.repository.ActivityLogRepository;
import com.sprintstack.repository.TaskRepository;
import com.sprintstack.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;

@Service
public class DashboardService {
    private final ProjectService projectService;
    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final ActivityLogRepository activity;
    private final NotificationRepository notifications;

    public DashboardService(ProjectService projectService, ProjectRepository projects, TaskRepository tasks, ActivityLogRepository activity, NotificationRepository notifications) {
        this.projectService=projectService; this.projects=projects; this.tasks=tasks; this.activity=activity; this.notifications=notifications;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary(String email) {
        User user = projectService.userByEmail(email);
        List<Project> visibleProjects = projects.findVisibleToUser(user.getId());
        long completedTasks = 0;
        long totalTasks = 0;
        List<DashboardResponse.ProjectProgress> progress = new java.util.ArrayList<>();
        Map<Long,DashboardResponse.TeamWorkload> workload = new HashMap<>();
        for (Project project : visibleProjects) {
            long total = tasks.countByProject_Id(project.getId());
            long completed = tasks.countByProject_IdAndStatus(project.getId(), TaskStatus.COMPLETED);
            totalTasks += total;
            completedTasks += completed;
            int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
            progress.add(new DashboardResponse.ProjectProgress(project.getId(), project.getName(), project.getStatus().name(), total, completed, percent));
            for(ProjectMember member:project.getMembers()){
                long assigned=tasks.countByProject_IdAndAssignee_IdAndStatusNot(project.getId(),member.getUser().getId(),TaskStatus.COMPLETED);
                workload.merge(member.getUser().getId(),new DashboardResponse.TeamWorkload(member.getUser().getId(),member.getUser().getFullName(),assigned),
                        (left,right)->new DashboardResponse.TeamWorkload(left.userId(),left.fullName(),left.assignedTasks()+right.assignedTasks()));
            }
        }
        long active = visibleProjects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        long completedProjects = visibleProjects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        long overdue = tasks.countOverdueForMember(user.getId(), LocalDate.now(), TaskStatus.COMPLETED);
        long dueSoon=tasks.countDueSoonForMember(user.getId(),LocalDate.now(),LocalDate.now().plusDays(7),TaskStatus.COMPLETED);
        EnumMap<TaskStatus,Long> statusCounts=new EnumMap<>(TaskStatus.class);
        for(TaskStatus status:TaskStatus.values())statusCounts.put(status,0L);
        for(TaskRepository.StatusCount count:tasks.countStatusesForMember(user.getId()))statusCounts.put(count.getStatus(),count.getAmount());
        LocalDate weekStart=LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(5);
        Map<LocalDate,Long> weekly=new HashMap<>();
        List<Task> recentlyCompleted=tasks.findCompletedSinceForMember(user.getId(),TaskStatus.COMPLETED,weekStart.atStartOfDay());
        for(Task task:recentlyCompleted){LocalDate key=task.getCompletedAt().toLocalDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));weekly.merge(key,1L,Long::sum);}
        List<DashboardResponse.WeeklyCompletion> weeklyCompletion=new ArrayList<>();
        for(int i=0;i<6;i++){LocalDate week=weekStart.plusWeeks(i);weeklyCompletion.add(new DashboardResponse.WeeklyCompletion(week.toString(),weekly.getOrDefault(week,0L)));}
        List<DashboardResponse.TeamWorkload> teamWorkload=workload.values().stream().sorted(Comparator.comparingLong(DashboardResponse.TeamWorkload::assignedTasks).reversed()).toList();
        Map<String,Long> statusMap=new LinkedHashMap<>();statusCounts.forEach((key,value)->statusMap.put(key.name(),value));
        List<Long> projectIds=visibleProjects.stream().map(Project::getId).toList();
        List<ActivityResponse> recentActivity=projectIds.isEmpty()?List.of():activity.findTop10ByProject_IdInOrderByCreatedAtDesc(projectIds)
                .stream().map(ActivityResponse::from).toList();
        List<com.sprintstack.dto.notification.NotificationResponse> recentNotifications=notifications.findTop5ByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream().map(com.sprintstack.dto.notification.NotificationResponse::from).toList();
        return new DashboardResponse(visibleProjects.size(), active, completedProjects, totalTasks, completedTasks,
                totalTasks - completedTasks, overdue, dueSoon, statusMap, weeklyCompletion, teamWorkload, progress, recentActivity, recentNotifications);
    }
}
