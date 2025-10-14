package com.aruba.customeranalysis.domain;

import com.aruba.customeranalysis.domain.model.ServiceType;

public interface EmailSenderInterface {
	
    void sendUpsellingEmail(String customerId, ServiceType serviceType);
}