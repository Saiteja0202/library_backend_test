package com.cts.library.service;

import com.cts.library.model.Notification;
import com.cts.library.model.NotificationStage;
import com.cts.library.repository.NotificationRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

	@PersistenceContext
	private EntityManager entityManager;

    private final NotificationRepo notificationRepository;
    private final JavaMailSender mailSender;

    public NotificationServiceImpl(NotificationRepo notificationRepository,
                                   JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void generateBorrowAcknowledgements() {
    	entityManager.createNativeQuery("SET SQL_SAFE_UPDATES = 0").executeUpdate();
    	
        int created = notificationRepository.insertBorrowAcknowledgements();
        System.out.println("Created Borrow notifications: " + created);

        List<Notification> notifications = notificationRepository.findByStage(NotificationStage.CREATED);
        sendBatch(notifications, "Borrowed Successfully");
        notifications.forEach(n -> {
            n.setStage(NotificationStage.REMINDER);
            notificationRepository.save(n);
        });
    }


    @Scheduled(cron = "00 58 14 * * *")
    public void generateDueAndOverdueNotifications() {

        int reminderUpdate = notificationRepository.updateDueReminders();
        int urgentUpdate = notificationRepository.updateReminderToDueTodayMessage();
        int overdueUpdate = notificationRepository.upgradeReminderToOverdue();


        System.out.println("Reminder Updated: " + reminderUpdate);
        System.out.println("Urgent Updated: " + urgentUpdate);
        System.out.println("Overdue Updated: " + overdueUpdate);


        sendBatch(notificationRepository.findByStage(NotificationStage.REMINDER), "Reminder:");
        sendBatch(notificationRepository.findByStage(NotificationStage.URGENT), "Urgent Reminder:");
        sendBatch(notificationRepository.findByStage(NotificationStage.OVERDUE), "Overdue:");
       
    }

    private void sendBatch(List<Notification> notifications, String subjectPrefix) {
        for (Notification notification : notifications) {
            String toEmail = (notification.getMember() != null) ? notification.getMember().getEmail() : null;
            if (toEmail == null) continue;

            SimpleMailMessage sms = new SimpleMailMessage();
            sms.setTo(toEmail);
            sms.setSubject(subjectPrefix + " " + extractSubjectSnippet(notification.getMessage()));
            sms.setText(notification.getMessage());


            mailSender.send(sms);
            System.out.println("Sent to: " + toEmail + " | Subject: " + subjectPrefix);
        }
    }
    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void generateReturnAcknowledgements() {
        List<Notification> paidNotifications = notificationRepository.findByStage(NotificationStage.PAID);

        sendBatch(paidNotifications, "Returned Successfully");

        paidNotifications.forEach(n -> {
        	n.setStage(NotificationStage.COMPLETED);
            notificationRepository.save(n);
        });

        System.out.println("PAID notifications sent and updated.");
    }
    
    
    private String extractSubjectSnippet(String message) {
        if (message == null || message.isBlank()) return "";
        int quoteStart = message.indexOf('"');
        int quoteEnd = message.indexOf('"', quoteStart + 1);
        if (quoteStart != -1 && quoteEnd != -1) {
            return message.substring(quoteStart, quoteEnd + 1); 
        }
        return ""; 
    }
}
