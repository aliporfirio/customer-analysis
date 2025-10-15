package com.aruba.customeranalysis.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.model.ServiceType;
import com.aruba.customeranalysis.infrastructure.persistence.model.CustomerServiceEntity;

public interface JpaCustomerServiceRepository extends JpaRepository<CustomerServiceEntity, UUID> {
	
	Optional<CustomerServiceEntity> findByCustomerIdAndServiceTypeAndActivationDate(String customerId, 
			ServiceType serviceType, LocalDate activationDate);
	
	@Query("""
	        SELECT new com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO(
	            c.serviceType, COUNT(c)
	        )
	        FROM CustomerServiceEntity c
	        WHERE c.status = com.aruba.customeranalysis.domain.model.ServiceStatus.ACTIVE
	        GROUP BY c.serviceType
	        """)
    List<ServiceSummaryDTO> findActiveServicesByType();

    @Query("""
        SELECT new com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO(
            c.customerId, AVG(c.amount)
        )
        FROM CustomerServiceEntity c
        GROUP BY c.customerId
        """)
    List<CustomerAverageCostDTO> findAverageCostPerCustomer();

    @Query("""
        SELECT new com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO(
            c.customerId, COUNT(c)
        )
        FROM CustomerServiceEntity c
        WHERE c.status = com.aruba.customeranalysis.domain.model.ServiceStatus.EXPIRED
        GROUP BY c.customerId
        HAVING COUNT(c) > 1
        """)
    List<CustomerExpiredServiceDTO> findCustomersWithMultipleExpired();

    @Query("""
        SELECT new com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO(
            c.customerId, c.serviceType, c.expirationDate
        )
        FROM CustomerServiceEntity c
        WHERE c.expirationDate <= :limitDate
          AND c.status = com.aruba.customeranalysis.domain.model.ServiceStatus.ACTIVE
        """)
    List<CustomerExpiringServiceDTO> findCustomersExpiringSoon(@Param("limitDate") LocalDate limitDate);
    
    @Query("""
	    SELECT c
	    FROM CustomerServiceEntity c
	    WHERE c.status = com.aruba.customeranalysis.domain.model.ServiceStatus.ACTIVE
	      AND c.activationDate <= :limitDate
	""")
	List<CustomerServiceEntity> findActiveServicesOlderThan(@Param("limitDate") LocalDate limitDate);
	
}
