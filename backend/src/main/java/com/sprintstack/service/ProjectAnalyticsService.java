package com.sprintstack.service;

import com.sprintstack.dto.analytics.ProjectAnalyticsResponse;
import com.sprintstack.entity.*;
import com.sprintstack.repository.IssueRepository;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectAnalyticsService {
    private final ProjectService projects;
    private final TaskRepository tasks;
    private final IssueRepository issues;
    private final ProjectMemberRepository members;

    public ProjectAnalyticsService(ProjectService projects, TaskRepository tasks, IssueRepository issues,
                                   ProjectMemberRepository members) {
        this.projects = projects;
        this.tasks = tasks;
        this.issues = issues;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse summary(String email, Long projectId) {
        projects.projectForMember(email, projectId);
        List<Task> projectTasks = tasks.findAllByProject_Id(projectId);
        List<Issue> projectIssues = issues.findAllByProject_Id(projectId);
        EnumMap<TaskStatus, Long> byStatus = zeroCounts(TaskStatus.values());
        EnumMap<TaskPriority, Long> byPriority = zeroCounts(TaskPriority.values());
        EnumMap<IssueSeverity, Long> bySeverity = zeroCounts(IssueSeverity.values());
        Map<Long, Long> workloadCounts = new HashMap<>();
        long overdue = 0;
        LocalDate today = LocalDate.now();
        for (Task task : projectTasks) {
            byStatus.merge(task.getStatus(), 1L, Long::sum);
            byPriority.merge(task.getPriority(), 1L, Long::sum);
            if (task.getDueDate() != null && task.getDueDate().isBefore(today) && task.getStatus() != TaskStatus.COMPLETED) overdue++;
            if (task.getAssignee() != null && task.getStatus() != TaskStatus.COMPLETED)
                workloadCounts.merge(task.getAssignee().getId(), 1L, Long::sum);
        }
        for (Issue issue : projectIssues) bySeverity.merge(issue.getSeverity(), 1L, Long::sum);
        long total = projectTasks.size();
        long completed = byStatus.get(TaskStatus.COMPLETED);
        List<ProjectAnalyticsResponse.MemberWorkload> workload = members.findAllByProject_IdOrderByJoinedAtAsc(projectId)
                .stream().map(member -> new ProjectAnalyticsResponse.MemberWorkload(
                        member.getUser().getId(), member.getUser().getFullName(), workloadCounts.getOrDefault(member.getUser().getId(), 0L)))
                .sorted(Comparator.comparingLong(ProjectAnalyticsResponse.MemberWorkload::openAssignedTasks).reversed()
                        .thenComparing(ProjectAnalyticsResponse.MemberWorkload::fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new ProjectAnalyticsResponse(total, completed, total - completed, byStatus.get(TaskStatus.IN_PROGRESS),
                overdue, total == 0 ? 0 : (int) Math.round(completed * 100.0 / total), names(byStatus), names(byPriority),
                names(bySeverity), workload);
    }

    private static <E extends Enum<E>> EnumMap<E, Long> zeroCounts(E[] values) {
        EnumMap<E, Long> result = new EnumMap<>(values[0].getDeclaringClass());
        for (E value : values) result.put(value, 0L);
        return result;
    }

    private static <E extends Enum<E>> Map<String, Long> names(EnumMap<E, Long> counts) {
        return counts.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue,
                (first, second) -> first, LinkedHashMap::new));
    }
}
