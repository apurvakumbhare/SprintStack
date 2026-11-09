package com.sprintstack.repository;

import com.sprintstack.entity.Task;
import com.sprintstack.entity.TaskStatus;
import com.sprintstack.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select t from Task t where t.project.id = :projectId and (:status is null or t.status = :status) and (:priority is null or t.priority = :priority) and (:assigneeId is null or t.assignee.id = :assigneeId) and (:overdue = false or (t.dueDate < :today and t.status <> :completed)) and (:query is null or lower(t.title) like lower(concat('%', :query, '%')) or lower(t.description) like lower(concat('%', :query, '%')) or exists (select label from Task taskWithLabel join taskWithLabel.labels label where taskWithLabel.id = t.id and lower(label) like lower(concat('%', :query, '%')))) order by t.dueDate asc, t.createdAt desc")
    List<Task> searchProjectTasks(@Param("projectId") Long projectId, @Param("status") TaskStatus status,
                                  @Param("priority") TaskPriority priority,@Param("assigneeId") Long assigneeId,
                                  @Param("overdue") boolean overdue,@Param("today") LocalDate today,
                                  @Param("completed") TaskStatus completed,@Param("query") String query);

    long countByProject_Id(Long projectId);
    long countByProject_IdAndStatus(Long projectId, TaskStatus status);
    long countByProject_IdAndAssignee_Id(Long projectId, Long assigneeId);
    long countByProject_IdAndAssignee_IdAndStatusNot(Long projectId, Long assigneeId, TaskStatus status);
    List<Task> findAllByProject_Id(Long projectId);
    List<Task> findAllByDueDateAndAssigneeIsNotNullAndStatusNot(LocalDate dueDate,TaskStatus status);
    List<Task> findAllByDueDateBeforeAndAssigneeIsNotNullAndStatusNot(LocalDate date,TaskStatus status);

    interface StatusCount { TaskStatus getStatus(); long getAmount(); }
    @Query("select t.status as status, count(distinct t.id) as amount from Task t join t.project.members m where m.user.id = :userId group by t.status")
    List<StatusCount> countStatusesForMember(@Param("userId") Long userId);

    @Query("select distinct t from Task t join t.project.members m where m.user.id = :userId and t.status = :status and t.completedAt >= :since")
    List<Task> findCompletedSinceForMember(@Param("userId") Long userId,@Param("status") TaskStatus status,@Param("since") LocalDateTime since);

    @Query("select count(distinct t.id) from Task t join t.project.members m where m.user.id = :userId and t.dueDate between :today and :lastDay and t.status <> :completed")
    long countDueSoonForMember(@Param("userId") Long userId,@Param("today") LocalDate today,@Param("lastDay") LocalDate lastDay,@Param("completed") TaskStatus completed);

    @Query("select count(t) from Task t join t.project.members m where m.user.id = :userId and t.dueDate < :today and t.status <> :completed")
    long countOverdueForMember(@Param("userId") Long userId, @Param("today") LocalDate today,
                               @Param("completed") TaskStatus completed);
}
