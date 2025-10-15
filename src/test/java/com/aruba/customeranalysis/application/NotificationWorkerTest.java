package com.aruba.customeranalysis.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aruba.customeranalysis.domain.EmailSenderInterface;
import com.aruba.customeranalysis.domain.NotificationRepositoryInterface;
import com.aruba.customeranalysis.domain.event.CustomerEvent;
import com.aruba.customeranalysis.domain.job.NotificationJob;
import com.aruba.customeranalysis.domain.model.Notification;
import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.domain.model.ServiceType;
import com.aruba.customeranalysis.infrastructure.messaging.EventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerTest {

    @Mock
    private NotificationRepositoryInterface notificationRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EmailSenderInterface emailSender;

    @InjectMocks
    private NotificationWorker worker;

    @BeforeEach
    void setup() {

    	ReflectionTestUtils.setField(worker, "expiredMaxRetries", 3);
        ReflectionTestUtils.setField(worker, "upsellingMaxRetries", 2);
    }

    @Test
    void process_shouldHandleExpiredAlertAndPublishEventSuccessfully() {
   
        NotificationJob job = new NotificationJob(
                NotificationType.EXPIRED_ALERT,
                "CUST001",
                5L,
                null
        );

        worker.process(job);

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        verify(eventPublisher, times(1)).publish(any(CustomerEvent.class));
    }

    @Test
    void process_shouldRetryAndEventuallyFailExpiredAlert() {
        
        NotificationJob job = new NotificationJob(
                NotificationType.EXPIRED_ALERT,
                "CUST002",
                2L,
                null
        );
        
        doThrow(new RuntimeException("Send failed")).when(eventPublisher).publish(any(CustomerEvent.class));

        worker.process(job);

        verify(eventPublisher, times(3)).publish(any(CustomerEvent.class)); 
        verify(notificationRepository, atLeast(3)).save(any(Notification.class));
    }

    @Test
    void process_shouldSendUpsellEmailSuccessfully() {
        
        NotificationJob job = new NotificationJob(
                NotificationType.UPSELL_EMAIL,
                "CUST003",
                null,
                ServiceType.PEC
        );

        worker.process(job);

        verify(emailSender, times(1)).sendUpsellingEmail("CUST003", ServiceType.PEC);
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void process_shouldRetryAndFailUpsellEmail() {
       
        NotificationJob job = new NotificationJob(
                NotificationType.UPSELL_EMAIL,
                "CUST004",
                null,
                ServiceType.HOSTING
        );
        
        doThrow(new RuntimeException("SMTP error")).when(emailSender).sendUpsellingEmail(any(), any());

        worker.process(job);

        verify(emailSender, times(2)).sendUpsellingEmail(any(), any());
        verify(notificationRepository, atLeast(2)).save(any(Notification.class));
    }

}
