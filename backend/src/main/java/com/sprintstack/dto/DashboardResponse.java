package com.sprintstack.dto;

import java.util.List;
import java.util.Map;
import com.sprintstack.dto.activity.ActivityResponse;

public record DashboardResponse(long totalProjects, long activeProjects, long completedProjects,
                                long totalTasks, long completedTasks, long pendingTasks,
                                long overdueTasks, long dueSoonTasks, Map<String,Long> taskStatusCounts,
                                List<WeeklyCompletion> weeklyCompletion, List<TeamWorkload> teamWorkload,
                                List<ProjectProgress> projectProgress, List<ActivityResponse> recentActivity) {
    public record ProjectProgress(Long id, String name, String status, long totalTasks,
                                  long completedTasks, int completionPercent) { }
    public record WeeklyCompletion(String weekStarting, long completedTasks) { }
    public record TeamWorkload(Long userId, String fullName, long assignedTasks) { }
}
