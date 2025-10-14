package com.aruba.customeranalysis.domain.job;

import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.domain.model.ServiceType;

public record NotificationJob(
        NotificationType type,
        String customerId,
        Long expiredCount,
        ServiceType serviceType
) {}
