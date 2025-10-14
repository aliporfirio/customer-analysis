package com.aruba.customeranalysis.domain.dto;

import com.aruba.customeranalysis.domain.model.ServiceType;

public class ServiceSummaryDTO {

    private ServiceType serviceType;
    private Long activeCount;

    public ServiceSummaryDTO(ServiceType serviceType, Long activeCount) {
        this.serviceType = serviceType;
        this.activeCount = activeCount;
    }

    public ServiceType getServiceType() { 
    	return serviceType; 
	}
    
    public void setServiceType(ServiceType serviceType) { 
    	this.serviceType = serviceType; 
    }

    public Long getActiveCount() { 
    	return activeCount; 
    }
    
    public void setActiveCount(Long activeCount) { 
    	this.activeCount = activeCount; 
    }
}
