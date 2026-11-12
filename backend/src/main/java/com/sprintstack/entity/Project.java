package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(length = 2000)
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "github_repo_url", length = 300)
    private String githubRepoUrl;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "project")
    private Set<ProjectMember> members = new LinkedHashSet<>();

    protected Project() { }
    public Project(String name, String description, ProjectStatus status, Priority priority,
                   LocalDate startDate, LocalDate dueDate, User createdBy) {
        this.name = name; this.description = description; this.status = status; this.priority = priority;
        this.startDate = startDate; this.dueDate = dueDate; this.createdBy = createdBy;
    }
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
    public void update(String name, String description, ProjectStatus status, Priority priority,
                       LocalDate startDate, LocalDate dueDate) {
        this.name=name; this.description=description; this.status=status; this.priority=priority;
        this.startDate=startDate; this.dueDate=dueDate;
        this.updatedAt=LocalDateTime.now();
    }
    public void archive() { this.status = ProjectStatus.ARCHIVED; this.updatedAt=LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Set<ProjectMember> getMembers() { return members; }
    public void updateGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; this.updatedAt = LocalDateTime.now(); }
}
