package com.gamemarket.controller;

import com.gamemarket.entity.Notification;
import com.gamemarket.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getNotifications(@RequestParam Integer userId) {
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @PostMapping("/read")
    public Map<String, Object> markAsRead(@RequestBody Map<String, Object> payload) {
        Integer notificationId = Integer.parseInt(payload.get("id").toString());
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        n.setIsRead(true);
        notificationRepository.save(n);
        return Map.of("message", "Marked as read");
    }
    
    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead(@RequestBody Map<String, Object> payload) {
        Integer userId = Integer.parseInt(payload.get("userId").toString());
        List<Notification> list = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        for (Notification n : list) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        }
        return Map.of("message", "All marked as read");
    }
}
