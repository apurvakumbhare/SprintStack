package com.sprintstack.repository;

import com.sprintstack.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("select distinct p from Project p join p.members m where m.user.id = :userId order by p.updatedAt desc")
    List<Project> findVisibleToUser(@Param("userId") Long userId);
}
