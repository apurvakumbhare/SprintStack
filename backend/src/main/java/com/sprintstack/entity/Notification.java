package com.sprintstack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="notifications",indexes=@Index(name="idx_notifications_user_read",columnList="user_id,is_read,created_at"))
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(name="notification_type",nullable=false,length=40) private String type;
    @Column(nullable=false,length=500) private String message;
    @Column(name="related_entity_type",length=30) private String relatedEntityType;
    @Column(name="related_entity_id") private Long relatedEntityId;
    @Column(name="is_read",nullable=false) private boolean read;
    @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
    protected Notification(){}
    public Notification(User user,String type,String message,String entityType,Long entityId){this.user=user;this.type=type;this.message=message;this.relatedEntityType=entityType;this.relatedEntityId=entityId;this.read=false;}
    @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
    public void markRead(){read=true;}
    public Long getId(){return id;} public User getUser(){return user;} public String getType(){return type;} public String getMessage(){return message;}
    public String getRelatedEntityType(){return relatedEntityType;} public Long getRelatedEntityId(){return relatedEntityId;} public boolean isRead(){return read;} public LocalDateTime getCreatedAt(){return createdAt;}
}
