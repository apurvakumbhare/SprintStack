package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues", indexes = {@Index(name = "idx_issues_project_status", columnList = "project_id,status"), @Index(name = "idx_issues_assignee", columnList = "assigned_to")})
public class Issue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 180) private String title;
    @Column(length = 4000) private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false) private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reported_by", nullable = false) private User reporter;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to") private User assignee;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private IssueSeverity severity = IssueSeverity.MEDIUM;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TaskPriority priority = TaskPriority.MEDIUM;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private IssueStatus status = IssueStatus.OPEN;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected Issue() { }
    public Issue(String title, String description, Project project, User reporter, User assignee,
                 IssueSeverity severity, TaskPriority priority) {
        this.title=title; this.description=description; this.project=project; this.reporter=reporter; this.assignee=assignee;
        this.severity=severity; this.priority=priority; this.status=IssueStatus.OPEN;
    }
    @PrePersist void onCreate() { createdAt=LocalDateTime.now(); updatedAt=createdAt; }
    @PreUpdate void onUpdate() { updatedAt=LocalDateTime.now(); }
    public void update(String title, String description, User assignee, IssueSeverity severity, TaskPriority priority, IssueStatus status) {
        this.title=title; this.description=description; this.assignee=assignee; this.severity=severity; this.priority=priority; this.status=status; this.updatedAt=LocalDateTime.now();
    }
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;}
    public Project getProject(){return project;} public User getReporter(){return reporter;} public User getAssignee(){return assignee;}
    public IssueSeverity getSeverity(){return severity;} public TaskPriority getPriority(){return priority;} public IssueStatus getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
