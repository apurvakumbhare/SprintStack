package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="activity_logs",indexes=@Index(name="idx_activity_project_created",columnList="project_id,created_at"))
public class ActivityLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id") private Project project;
    @Column(name="action_type",nullable=false,length=50) private String actionType;
    @Column(name="related_entity_type",length=30) private String relatedEntityType;
    @Column(name="related_entity_id") private Long relatedEntityId;
    @Column(nullable=false,length=500) private String description;
    @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
    protected ActivityLog(){}
    public ActivityLog(User user,Project project,String actionType,String entityType,Long entityId,String description){this.user=user;this.project=project;this.actionType=actionType;this.relatedEntityType=entityType;this.relatedEntityId=entityId;this.description=description;}
    @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public User getUser(){return user;} public Project getProject(){return project;}
    public String getActionType(){return actionType;} public String getRelatedEntityType(){return relatedEntityType;}
    public Long getRelatedEntityId(){return relatedEntityId;} public String getDescription(){return description;} public LocalDateTime getCreatedAt(){return createdAt;}
}
