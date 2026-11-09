package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tasks", indexes = {@Index(name = "idx_tasks_project_status", columnList = "project_id,status"), @Index(name = "idx_tasks_assignee", columnList = "assigned_to")})
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(length = 4000)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to")
    private User assignee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "estimated_effort") private Integer estimatedEffort;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_labels", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "label", nullable = false, length = 60)
    private Set<String> labels = new LinkedHashSet<>();

    protected Task() { }
    public Task(String title, String description, Project project, User assignee, User createdBy,
                TaskPriority priority, TaskStatus status, LocalDate startDate, LocalDate dueDate, Integer estimatedEffort) {
        this.title=title; this.description=description; this.project=project; this.assignee=assignee; this.createdBy=createdBy;
        this.priority=priority; this.status=status; this.startDate=startDate; this.dueDate=dueDate; this.estimatedEffort=estimatedEffort;
    }
    @PrePersist void onCreate() { createdAt=LocalDateTime.now(); updatedAt=createdAt; }
    @PreUpdate void onUpdate() { updatedAt=LocalDateTime.now(); }
    public void update(String title, String description, User assignee, TaskPriority priority, TaskStatus status,
                       LocalDate startDate, LocalDate dueDate, Integer estimatedEffort, Set<String> labels) {
        TaskStatus previousStatus=this.status;
        this.title=title; this.description=description; this.assignee=assignee; this.priority=priority; this.status=status;
        this.startDate=startDate; this.dueDate=dueDate; this.estimatedEffort=estimatedEffort;
        this.labels.clear(); this.labels.addAll(labels);
        if(status==TaskStatus.COMPLETED&&previousStatus!=TaskStatus.COMPLETED)this.completedAt=LocalDateTime.now();
        if(status!=TaskStatus.COMPLETED)this.completedAt=null;
        this.updatedAt=LocalDateTime.now();
    }
    public void changeStatus(TaskStatus status) {
        if(status==TaskStatus.COMPLETED&&this.status!=TaskStatus.COMPLETED)this.completedAt=LocalDateTime.now();
        if(status!=TaskStatus.COMPLETED)this.completedAt=null;
        this.status=status; this.updatedAt=LocalDateTime.now();
    }
    public void setLabels(Set<String> labels) { this.labels.clear(); this.labels.addAll(labels); }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Project getProject() { return project; }
    public User getAssignee() { return assignee; }
    public User getCreatedBy() { return createdBy; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Set<String> getLabels() { return Set.copyOf(labels); }
}
