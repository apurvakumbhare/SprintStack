package com.sprintstack.repository;
import com.sprintstack.entity.ActivityLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long>{
    @EntityGraph(attributePaths={"user","project"})
    List<ActivityLog> findTop50ByProject_IdOrderByCreatedAtDesc(Long projectId);
    @EntityGraph(attributePaths={"user","project"})
    List<ActivityLog> findTop10ByProject_IdInOrderByCreatedAtDesc(List<Long> projectIds);
    @EntityGraph(attributePaths={"user","project"})
    List<ActivityLog> findTop100ByProject_IdInOrderByCreatedAtDesc(List<Long> projectIds);
}
