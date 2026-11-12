package com.sprintstack.dto.comment;
import com.sprintstack.entity.TaskComment;
import com.sprintstack.entity.IssueComment;
import java.time.LocalDateTime;
import java.util.Base64;
public record CommentResponse(Long id,Long authorId,String authorName,String body,LocalDateTime createdAt,LocalDateTime updatedAt,String profilePhoto){
 public static CommentResponse from(TaskComment c){byte[] photo=c.getAuthor().getProfilePhoto();String profilePhoto=photo==null?null:"data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(photo);return new CommentResponse(c.getId(),c.getAuthor().getId(),c.getAuthor().getFullName(),c.getBody(),c.getCreatedAt(),c.getUpdatedAt(),profilePhoto);}
 public static CommentResponse from(IssueComment c){byte[] photo=c.getAuthor().getProfilePhoto();String profilePhoto=photo==null?null:"data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(photo);return new CommentResponse(c.getId(),c.getAuthor().getId(),c.getAuthor().getFullName(),c.getBody(),c.getCreatedAt(),c.getUpdatedAt(),profilePhoto);}
}
