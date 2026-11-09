package com.sprintstack.controller;

import com.sprintstack.dto.notification.NotificationResponse;
import com.sprintstack.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications){this.notifications=notifications;}
    @GetMapping public List<NotificationResponse> list(Authentication auth){return notifications.list(auth.getName());}
    @GetMapping("/unread-count") public Map<String,Long> unreadCount(Authentication auth){return Map.of("count",notifications.unreadCount(auth.getName()));}
    @PatchMapping("/{id}/read") public NotificationResponse markRead(Authentication auth,@PathVariable Long id){return notifications.markRead(auth.getName(),id);}
    @PatchMapping("/read-all") public Map<String,Integer> markAllRead(Authentication auth){return Map.of("updated",notifications.markAllRead(auth.getName()));}
}
