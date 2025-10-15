package com.aruba.customeranalysis.domain;

import java.time.LocalDate;
import java.util.List;

import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.ServiceType;

public interface CustomerServiceRepositoryInterface {
	
	CustomerService save(CustomerService customer);
	
	CustomerService saveOrUpdate(CustomerService service);

	List<ServiceSummaryDTO> findActiveServicesByType();

	List<CustomerAverageCostDTO> findAverageCostPerCustomer();

	List<CustomerExpiredServiceDTO> findCustomersWithExpiredServices();

	List<CustomerExpiringServiceDTO> findCustomersWithExpiringServices(LocalDate limitDate);

	List<CustomerService> findActiveServicesOlderThan(LocalDate limitDate);

	CustomerService findServicesByCustomerIdAndServiceTypeAndActivationDate(String customerId,
			ServiceType serviceType, LocalDate activationDate);

}
