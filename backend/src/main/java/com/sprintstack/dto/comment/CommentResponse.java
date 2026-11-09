package com.sprintstack.dto.comment;
import com.sprintstack.entity.TaskComment;
import com.sprintstack.entity.IssueComment;
import java.time.LocalDateTime;
public record CommentResponse(Long id,Long authorId,String authorName,String body,LocalDateTime createdAt,LocalDateTime updatedAt){
 public static CommentResponse from(TaskComment c){return new CommentResponse(c.getId(),c.getAuthor().getId(),c.getAuthor().getFullName(),c.getBody(),c.getCreatedAt(),c.getUpdatedAt());}
 public static CommentResponse from(IssueComment c){return new CommentResponse(c.getId(),c.getAuthor().getId(),c.getAuthor().getFullName(),c.getBody(),c.getCreatedAt(),c.getUpdatedAt());}
}
