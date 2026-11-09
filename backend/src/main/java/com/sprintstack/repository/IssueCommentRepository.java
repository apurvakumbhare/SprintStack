package com.sprintstack.repository;
import com.sprintstack.entity.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IssueCommentRepository extends JpaRepository<IssueComment,Long>{List<IssueComment> findAllByIssue_IdOrderByCreatedAtAsc(Long issueId);}
