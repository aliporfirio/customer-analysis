package com.aruba.customeranalysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CustomerService {

    private UUID id;
    private String customerId;
    private ServiceType serviceType;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private BigDecimal amount;
    private ServiceStatus status;
    
    public CustomerService() {}
    
	public CustomerService(UUID id, String customerId, ServiceType serviceType, LocalDate activationDate,
			LocalDate expirationDate, BigDecimal amount, ServiceStatus status) {

		this.id = id;
		this.customerId = customerId;
		this.serviceType = serviceType;
		this.activationDate = activationDate;
		this.expirationDate = expirationDate;
		this.amount = amount;
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public LocalDate getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(LocalDate activationDate) {
		this.activationDate = activationDate;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}
}
