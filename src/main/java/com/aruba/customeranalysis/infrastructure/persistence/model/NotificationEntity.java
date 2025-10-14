package com.aruba.customeranalysis.infrastructure.persistence.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.aruba.customeranalysis.domain.model.NotificationStatus;
import com.aruba.customeranalysis.domain.model.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {
	
	@Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    private String customerId;

    private Integer retryCount = 0;
    private OffsetDateTime createdDatetime = OffsetDateTime.now();
    private OffsetDateTime updatedDatetime;

}
