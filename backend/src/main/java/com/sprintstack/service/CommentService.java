package com.sprintstack.service;

import com.sprintstack.dto.comment.CommentRequest;
import com.sprintstack.dto.comment.CommentResponse;
import com.sprintstack.entity.*;
import com.sprintstack.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CommentService {
    private final TaskRepository tasks; private final IssueRepository issues;
    private final TaskCommentRepository taskComments; private final IssueCommentRepository issueComments;
    private final ProjectService projects;
    public CommentService(TaskRepository tasks,IssueRepository issues,TaskCommentRepository taskComments,
                          IssueCommentRepository issueComments,ProjectService projects){
        this.tasks=tasks;this.issues=issues;this.taskComments=taskComments;this.issueComments=issueComments;this.projects=projects;
    }
    @Transactional(readOnly=true)
    public List<CommentResponse> taskComments(String email,Long taskId){Task task=task(taskId);projects.requireMember(task.getProject().getId(),projects.userByEmail(email).getId());return taskComments.findAllByTask_IdOrderByCreatedAtAsc(taskId).stream().map(CommentResponse::from).toList();}
    @Transactional
    public CommentResponse addTaskComment(String email,Long taskId,CommentRequest request){Task task=task(taskId);User author=projects.userByEmail(email);projects.requireMember(task.getProject().getId(),author.getId());return CommentResponse.from(taskComments.save(new TaskComment(task,author,request.body().trim())));}
    @Transactional
    public CommentResponse editTaskComment(String email,Long taskId,Long commentId,CommentRequest request){User actor=projects.userByEmail(email);TaskComment comment=taskComments.findById(commentId).filter(c->c.getTask().getId().equals(taskId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));projects.requireMember(comment.getTask().getProject().getId(),actor.getId());requireAuthor(comment.getAuthor(),actor);comment.update(request.body().trim());return CommentResponse.from(comment);}
    @Transactional
    public void deleteTaskComment(String email,Long taskId,Long commentId){User actor=projects.userByEmail(email);TaskComment comment=taskComments.findById(commentId).filter(c->c.getTask().getId().equals(taskId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));projects.requireMember(comment.getTask().getProject().getId(),actor.getId());requireAuthor(comment.getAuthor(),actor);taskComments.delete(comment);}

    @Transactional(readOnly=true)
    public List<CommentResponse> issueComments(String email,Long issueId){Issue issue=issue(issueId);projects.requireMember(issue.getProject().getId(),projects.userByEmail(email).getId());return issueComments.findAllByIssue_IdOrderByCreatedAtAsc(issueId).stream().map(CommentResponse::from).toList();}
    @Transactional
    public CommentResponse addIssueComment(String email,Long issueId,CommentRequest request){Issue issue=issue(issueId);User author=projects.userByEmail(email);projects.requireMember(issue.getProject().getId(),author.getId());return CommentResponse.from(issueComments.save(new IssueComment(issue,author,request.body().trim())));}
    @Transactional
    public CommentResponse editIssueComment(String email,Long issueId,Long commentId,CommentRequest request){User actor=projects.userByEmail(email);IssueComment comment=issueComments.findById(commentId).filter(c->c.getIssue().getId().equals(issueId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));projects.requireMember(comment.getIssue().getProject().getId(),actor.getId());requireAuthor(comment.getAuthor(),actor);comment.update(request.body().trim());return CommentResponse.from(comment);}
    @Transactional
    public void deleteIssueComment(String email,Long issueId,Long commentId){User actor=projects.userByEmail(email);IssueComment comment=issueComments.findById(commentId).filter(c->c.getIssue().getId().equals(issueId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));projects.requireMember(comment.getIssue().getProject().getId(),actor.getId());requireAuthor(comment.getAuthor(),actor);issueComments.delete(comment);}

    private Task task(Long id){return tasks.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));}
    private Issue issue(Long id){return issues.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Issue not found"));}
    private void requireAuthor(User author,User actor){if(!author.getId().equals(actor.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only edit or delete your own comments");}
}
