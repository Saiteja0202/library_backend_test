package com.cts.library.controller;

import com.cts.library.model.Email;
import com.cts.library.model.Notification;
import com.cts.library.service.NotificationService;
import com.cts.library.service.NotificationServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.beans.JavaBean;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

	
	@Autowired
	private JavaMailSender sendMail;

    private final NotificationService notificationService;

 
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
       
    }

  

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); 
        }
    }

    @GetMapping("/trigger")
    public ResponseEntity<String> triggerNotificationsManually() {
        ((NotificationServiceImpl) notificationService).generateDueAndOverdueNotifications();
        return ResponseEntity.ok("Notifications triggered successfully.");
    }

    
    
    @PostMapping("/mail")
    public Email sendMail(@RequestBody Email email)
    {
    	SimpleMailMessage sms = new SimpleMailMessage();
    	sms.setTo(email.getTo());
    	sms.setSubject(email.getSubject());
    	sms.setText(email.getText());
    	sendMail.send(sms);
    	return email;
    }

}
