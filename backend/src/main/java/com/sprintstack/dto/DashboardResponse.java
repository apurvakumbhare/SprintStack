package com.sprintstack.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(long totalProjects, long activeProjects, long completedProjects,
                                long totalTasks, long completedTasks, long pendingTasks,
                                long overdueTasks, long dueSoonTasks, Map<String,Long> taskStatusCounts,
                                List<WeeklyCompletion> weeklyCompletion, List<TeamWorkload> teamWorkload,
                                List<ProjectProgress> projectProgress) {
    public record ProjectProgress(Long id, String name, String status, long totalTasks,
                                  long completedTasks, int completionPercent) { }
    public record WeeklyCompletion(String weekStarting, long completedTasks) { }
    public record TeamWorkload(Long userId, String fullName, long assignedTasks) { }
}
