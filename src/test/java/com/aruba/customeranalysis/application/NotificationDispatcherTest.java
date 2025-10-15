package com.aruba.customeranalysis.application;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aruba.customeranalysis.domain.job.NotificationJob;
import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.domain.model.ServiceType;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private NotificationWorker worker;

    @InjectMocks
    private NotificationDispatcher dispatcher;

    @Test
    void dispatch_shouldDelegateToWorkerProcess() {
        
        NotificationJob job = new NotificationJob(
                NotificationType.UPSELL_EMAIL,
                "CUST001",
                null,
                ServiceType.HOSTING
        );

        dispatcher.dispatch(job);

        verify(worker, times(1)).process(job);
    }

    @Test
    void dispatch_shouldNotThrowIfWorkerFails() {
        
        NotificationJob job = new NotificationJob(
                NotificationType.EXPIRED_ALERT,
                "CUST002",
                5L,
                null
        );
        
        doThrow(new RuntimeException("Processing failed")).when(worker).process(any());

        assertDoesNotThrow(() -> dispatcher.dispatch(job));
        verify(worker).process(job);
    }
}
