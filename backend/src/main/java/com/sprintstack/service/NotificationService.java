package com.sprintstack.service;

import com.sprintstack.dto.notification.NotificationResponse;
import com.sprintstack.entity.User;
import com.sprintstack.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final ProjectService projects;
    public NotificationService(NotificationRepository notifications,ProjectService projects){this.notifications=notifications;this.projects=projects;}
    @Transactional(readOnly=true)
    public List<NotificationResponse> list(String email){User user=projects.userByEmail(email);return notifications.findTop50ByUser_IdOrderByCreatedAtDesc(user.getId()).stream().map(NotificationResponse::from).toList();}
    @Transactional(readOnly=true)
    public long unreadCount(String email){return notifications.countByUser_IdAndReadFalse(projects.userByEmail(email).getId());}
    @Transactional
    public NotificationResponse markRead(String email,Long id){User user=projects.userByEmail(email);var notification=notifications.findByIdAndUser_Id(id,user.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));notification.markRead();return NotificationResponse.from(notification);}
    @Transactional
    public int markAllRead(String email){return notifications.markAllRead(projects.userByEmail(email).getId());}
}
