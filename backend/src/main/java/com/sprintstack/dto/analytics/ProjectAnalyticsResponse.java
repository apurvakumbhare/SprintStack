package com.sprintstack.dto.analytics;

import java.util.List;
import java.util.Map;

public record ProjectAnalyticsResponse(
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        long inProgressTasks,
        long overdueTasks,
        int completionPercent,
        Map<String, Long> tasksByStatus,
        Map<String, Long> tasksByPriority,
        Map<String, Long> issuesBySeverity,
        List<MemberWorkload> teamWorkload
) {
    public record MemberWorkload(Long userId, String fullName, long openAssignedTasks) { }
}
