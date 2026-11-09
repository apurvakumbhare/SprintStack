package com.sprintstack.repository;

import com.sprintstack.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import com.sprintstack.entity.ProjectRole;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);
    List<ProjectMember> findAllByProject_IdOrderByJoinedAtAsc(Long projectId);
    long countByProject_IdAndRole(Long projectId, ProjectRole role);
}
