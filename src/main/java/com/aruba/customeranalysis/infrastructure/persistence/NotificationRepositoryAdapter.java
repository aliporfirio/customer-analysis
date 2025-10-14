package com.aruba.customeranalysis.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.aruba.customeranalysis.domain.NotificationRepositoryInterface;
import com.aruba.customeranalysis.domain.model.Notification;
import com.aruba.customeranalysis.infrastructure.persistence.model.NotificationEntity;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepositoryInterface {

    private final JpaNotificationRepository jpaNotificationRepository;

    public NotificationRepositoryAdapter(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

	@Override
	public Notification save(Notification notification) {
		return toNotification(jpaNotificationRepository.save(toNotificationEntity(notification)));
	}
			
	private Notification toNotification(NotificationEntity notification) {
		
		return new Notification(
				notification.getId(),
				notification.getType(),
				notification.getStatus(),
				notification.getCustomerId(),
				notification.getRetryCount(),
				notification.getCreatedDatetime(),
				notification.getUpdatedDatetime());
		
    }
	
	private NotificationEntity toNotificationEntity(Notification notification) {
		
		return new NotificationEntity(
				notification.getId(),
				notification.getType(),
				notification.getStatus(),
				notification.getCustomerId(),
				notification.getRetryCount(),
				notification.getCreatedDatetime(),
				notification.getUpdatedDatetime());
		
    }

}
