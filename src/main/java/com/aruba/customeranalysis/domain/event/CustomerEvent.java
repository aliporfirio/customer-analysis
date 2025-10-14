package com.aruba.customeranalysis.domain.event;

import com.aruba.customeranalysis.domain.model.NotificationType;

public class CustomerEvent {
	
	private NotificationType notificationType;
	private String customerId;
	private Long expiredCount;
		
	public CustomerEvent() {}

	public CustomerEvent(NotificationType notificationType, String customerId, Long expiredCount) {

		this.notificationType = notificationType;
		this.customerId = customerId;
		this.expiredCount = expiredCount;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Long getExpiredCount() {
		return expiredCount;
	}

	public void setExpiredCount(Long expiredCount) {
		this.expiredCount = expiredCount;
	}
}
