package com.aruba.customeranalysis.domain.dto;

import java.time.LocalDate;

import com.aruba.customeranalysis.domain.model.ServiceType;

public class CustomerExpiringServiceDTO {

    private String customerId;
    private ServiceType serviceType;
    private LocalDate expirationDate;

    public CustomerExpiringServiceDTO(String customerId, ServiceType serviceType, LocalDate expirationDate) {
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.expirationDate = expirationDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
