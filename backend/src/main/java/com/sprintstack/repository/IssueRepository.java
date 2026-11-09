package com.sprintstack.repository;

import com.sprintstack.entity.Issue;
import com.sprintstack.entity.IssueStatus;
import com.sprintstack.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findAllByProject_Id(Long projectId);
    @Query("select i from Issue i where i.project.id = :projectId and (:status is null or i.status = :status) and (:severity is null or i.severity = :severity) and (:priority is null or i.priority = :priority) and (:query is null or lower(i.title) like lower(concat('%', :query, '%')) or lower(i.description) like lower(concat('%', :query, '%'))) order by i.updatedAt desc")
    List<Issue> searchProjectIssues(@Param("projectId") Long projectId, @Param("status") IssueStatus status,
                                    @Param("severity") com.sprintstack.entity.IssueSeverity severity,
                                    @Param("priority") TaskPriority priority,
                                    @Param("query") String query);
}
