package com.aruba.customeranalysis.domain.dto;

public class CustomerAverageCostDTO {

    private String customerId;
    private Double averageCost;

    public CustomerAverageCostDTO(String customerId, Double averageCost) {
    	
        this.customerId = customerId;
        this.averageCost = averageCost;
        
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Double getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(Double averageCost) {
        this.averageCost = averageCost;
    }
}
