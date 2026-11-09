package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="issue_comments",indexes=@Index(name="idx_issue_comments_issue",columnList="issue_id,created_at"))
public class IssueComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="issue_id",nullable=false) private Issue issue;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="author_id",nullable=false) private User author;
    @Column(nullable=false,length=4000) private String body;
    @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
    protected IssueComment(){}
    public IssueComment(Issue issue,User author,String body){this.issue=issue;this.author=author;this.body=body;}
    @PrePersist void onCreate(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
    @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
    public void update(String body){this.body=body;this.updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public Issue getIssue(){return issue;} public User getAuthor(){return author;}
    public String getBody(){return body;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
