package com.aruba.customeranalysis.infrastructure.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.aruba.customeranalysis.domain.model.ServiceStatus;
import com.aruba.customeranalysis.domain.model.ServiceType;

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
@Table(name = "customer_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceEntity {

	@Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private LocalDate activationDate;
    private LocalDate expirationDate;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status;
	
}
