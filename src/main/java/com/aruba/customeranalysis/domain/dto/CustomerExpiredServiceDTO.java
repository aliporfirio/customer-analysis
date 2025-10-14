package com.aruba.customeranalysis.domain.dto;

public class CustomerExpiredServiceDTO {

    private String customerId;
    private Long expiredServiceCount;

    public CustomerExpiredServiceDTO(String customerId, Long expiredServiceCount) {
    	
        this.customerId = customerId;
        this.expiredServiceCount = expiredServiceCount;
        
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getExpiredServiceCount() {
        return expiredServiceCount;
    }

    public void setExpiredServiceCount(Long expiredServiceCount) {
        this.expiredServiceCount = expiredServiceCount;
    }
}
