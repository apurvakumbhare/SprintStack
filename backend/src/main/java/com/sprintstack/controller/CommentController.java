package com.sprintstack.controller;

import com.sprintstack.dto.comment.CommentRequest;
import com.sprintstack.dto.comment.CommentResponse;
import com.sprintstack.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class CommentController {
    private final CommentService comments;
    public CommentController(CommentService comments){this.comments=comments;}
    @GetMapping("/api/tasks/{taskId}/comments") public List<CommentResponse> taskComments(Authentication auth,@PathVariable Long taskId){return comments.taskComments(auth.getName(),taskId);}
    @PostMapping("/api/tasks/{taskId}/comments") @ResponseStatus(HttpStatus.CREATED) public CommentResponse addTask(Authentication auth,@PathVariable Long taskId,@Valid @RequestBody CommentRequest request){return comments.addTaskComment(auth.getName(),taskId,request);}
    @PutMapping("/api/tasks/{taskId}/comments/{commentId}") public CommentResponse editTask(Authentication auth,@PathVariable Long taskId,@PathVariable Long commentId,@Valid @RequestBody CommentRequest request){return comments.editTaskComment(auth.getName(),taskId,commentId,request);}
    @DeleteMapping("/api/tasks/{taskId}/comments/{commentId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteTask(Authentication auth,@PathVariable Long taskId,@PathVariable Long commentId){comments.deleteTaskComment(auth.getName(),taskId,commentId);}
    @GetMapping("/api/issues/{issueId}/comments") public List<CommentResponse> issueComments(Authentication auth,@PathVariable Long issueId){return comments.issueComments(auth.getName(),issueId);}
    @PostMapping("/api/issues/{issueId}/comments") @ResponseStatus(HttpStatus.CREATED) public CommentResponse addIssue(Authentication auth,@PathVariable Long issueId,@Valid @RequestBody CommentRequest request){return comments.addIssueComment(auth.getName(),issueId,request);}
    @PutMapping("/api/issues/{issueId}/comments/{commentId}") public CommentResponse editIssue(Authentication auth,@PathVariable Long issueId,@PathVariable Long commentId,@Valid @RequestBody CommentRequest request){return comments.editIssueComment(auth.getName(),issueId,commentId,request);}
    @DeleteMapping("/api/issues/{issueId}/comments/{commentId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteIssue(Authentication auth,@PathVariable Long issueId,@PathVariable Long commentId){comments.deleteIssueComment(auth.getName(),issueId,commentId);}
}
