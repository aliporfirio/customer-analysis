package com.aruba.customeranalysis.infrastructure.persistence;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aruba.customeranalysis.domain.model.Notification;
import com.aruba.customeranalysis.domain.model.NotificationStatus;
import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.infrastructure.persistence.model.NotificationEntity;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock
    private JpaNotificationRepository jpaNotificationRepository;

    @InjectMocks
    private NotificationRepositoryAdapter adapter;

    @Test
    void save_shouldCallJpaRepositoryAndReturnMappedNotification() {
        
        Notification domainObj = new Notification(
                null,
                NotificationType.EXPIRED_ALERT,
                NotificationStatus.PENDING,
                "CUST001",
                1,
                OffsetDateTime.now(),
                null
        );
        
        UUID randomId = UUID.randomUUID();

        NotificationEntity savedEntity = new NotificationEntity(
        		randomId,
                domainObj.getType(),
                domainObj.getStatus(),
                domainObj.getCustomerId(),
                domainObj.getRetryCount(),
                domainObj.getCreatedDatetime(),
                domainObj.getUpdatedDatetime()
        );

        when(jpaNotificationRepository.save(any(NotificationEntity.class))).thenReturn(savedEntity);

        Notification result = adapter.save(domainObj);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(jpaNotificationRepository, times(1)).save(captor.capture());

        NotificationEntity captured = captor.getValue();
        assertEquals(domainObj.getCustomerId(), captured.getCustomerId());
        assertEquals(domainObj.getType(), captured.getType());
        assertEquals(domainObj.getStatus(), captured.getStatus());

        assertNotNull(result);
        assertEquals(randomId, result.getId());
        assertEquals(domainObj.getCustomerId(), result.getCustomerId());
        assertEquals(domainObj.getType(), result.getType());
        assertEquals(domainObj.getStatus(), result.getStatus());
    }
}
