package com.aruba.customeranalysis.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.customeranalysis.infrastructure.persistence.model.NotificationEntity;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {}
