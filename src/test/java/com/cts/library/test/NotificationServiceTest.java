package com.cts.library.test;

import com.cts.library.model.Notification;
import com.cts.library.repository.NotificationRepo;
import com.cts.library.service.NotificationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private NotificationRepo notificationRepo;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        notificationRepo = mock(NotificationRepo.class);
        service = new NotificationServiceImpl(notificationRepo, null);
    }


    @Test
    void testGetAllNotifications_ShouldReturnList() {
        Notification n1 = new Notification();
        Notification n2 = new Notification();
        when(notificationRepo.findAll()).thenReturn(Arrays.asList(n1, n2));

        List<Notification> result = service.getAllNotifications();
        assertEquals(2, result.size());
        System.out.println("Notifications retrieved.");
    }

    @Test
    void testGenerateDueAndOverdueNotifications_DynamicCounts() {
//        when(notificationRepo.updateDueReminders()).thenReturn(2);
//        when(notificationRepo.updateDueReminderMessages()).thenReturn(3);
        when(notificationRepo.updateReminderToDueTodayMessage()).thenReturn(2);
        when(notificationRepo.upgradeReminderToOverdue()).thenReturn(1);
        when(notificationRepo.upgradeOverdueToReturned()).thenReturn(4);

        assertDoesNotThrow(() -> service.generateDueAndOverdueNotifications());

//        verify(notificationRepo).updateDueReminders();
//        verify(notificationRepo).updateDueReminderMessages();
        verify(notificationRepo).updateReminderToDueTodayMessage();
        verify(notificationRepo).upgradeReminderToOverdue();
        verify(notificationRepo).upgradeOverdueToReturned();

        System.out.println("Scheduled updates executed.");
    }
}
