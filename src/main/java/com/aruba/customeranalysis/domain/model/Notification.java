package com.aruba.customeranalysis.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Notification {
	
    private UUID id;
    private NotificationType type;
    private NotificationStatus status;
    private String customerId;
    private Integer retryCount;
    private OffsetDateTime createdDatetime;
    private OffsetDateTime updatedDatetime;
    
    public Notification() {}

	public Notification(UUID id, NotificationType type, NotificationStatus status, String customerId,
			Integer retryCount, OffsetDateTime createdDatetime, OffsetDateTime updatedDatetime) {

		this.id = id;
		this.type = type;
		this.status = status;
		this.customerId = customerId;
		this.retryCount = retryCount;
		this.createdDatetime = createdDatetime;
		this.updatedDatetime = updatedDatetime;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}
	
	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public OffsetDateTime getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(OffsetDateTime createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public OffsetDateTime getUpdatedDatetime() {
		return updatedDatetime;
	}

	public void setUpdatedDatetime(OffsetDateTime updatedDatetime) {
		this.updatedDatetime = updatedDatetime;
	}

}
