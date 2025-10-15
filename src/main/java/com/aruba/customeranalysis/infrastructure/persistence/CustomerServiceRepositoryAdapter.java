package com.aruba.customeranalysis.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.ServiceType;
import com.aruba.customeranalysis.infrastructure.persistence.model.CustomerServiceEntity;

@Repository
public class CustomerServiceRepositoryAdapter implements CustomerServiceRepositoryInterface {

    private final JpaCustomerServiceRepository jpaCustomerServiceRepository;

    public CustomerServiceRepositoryAdapter(JpaCustomerServiceRepository jpaCustomerServiceRepository) {
        this.jpaCustomerServiceRepository = jpaCustomerServiceRepository;
    }

	@Override
	public CustomerService save(CustomerService service) {
		return toCustomerService(jpaCustomerServiceRepository.save(toCustomerServiceEntity(service)));
	}
	
	@Override
	public CustomerService saveOrUpdate(CustomerService service) {
		
		CustomerService existingService = findServicesByCustomerIdAndServiceTypeAndActivationDate(
				service.getCustomerId(), 
				service.getServiceType(), 
				service.getActivationDate());
		
		if (existingService != null) {
			
			existingService.setAmount(service.getAmount());
			existingService.setExpirationDate(service.getExpirationDate());
			existingService.setStatus(service.getStatus());
			
			return toCustomerService(jpaCustomerServiceRepository.save(toCustomerServiceEntity(existingService)));
			
		} else {		
			return toCustomerService(jpaCustomerServiceRepository.save(toCustomerServiceEntity(service)));
		}
	}
	
	@Override
	public CustomerService findServicesByCustomerIdAndServiceTypeAndActivationDate(
			String customerId, 
			ServiceType serviceType, 
			LocalDate activationDate) {
		
		Optional<CustomerServiceEntity> service = jpaCustomerServiceRepository.findByCustomerIdAndServiceTypeAndActivationDate(
				customerId, serviceType, activationDate);
	    
		if (service.isPresent()) {
			return toCustomerService(service.get());
		} else {
			return null;
		}
		
	}
	
	@Override
    public List<ServiceSummaryDTO> findActiveServicesByType() {
        return jpaCustomerServiceRepository.findActiveServicesByType();
    }

    @Override
    public List<CustomerAverageCostDTO> findAverageCostPerCustomer() {
        return jpaCustomerServiceRepository.findAverageCostPerCustomer();
    }

    @Override
    public List<CustomerExpiredServiceDTO> findCustomersWithExpiredServices() {
        return jpaCustomerServiceRepository.findCustomersWithMultipleExpired();
    }

    @Override
    public List<CustomerExpiringServiceDTO> findCustomersWithExpiringServices(LocalDate limitDate) {
        return jpaCustomerServiceRepository.findCustomersExpiringSoon(limitDate);
    }
    
    @Override
    public List<CustomerService> findActiveServicesOlderThan(LocalDate limitDate) {
    	
        List<CustomerServiceEntity> services = jpaCustomerServiceRepository.findActiveServicesOlderThan(limitDate);
       
        return services.stream()
                       .map(this::toCustomerService)
                       .toList();
        
    }
	
	private CustomerService toCustomerService(CustomerServiceEntity service) {
		
		return new CustomerService(
				service.getId(), 
				service.getCustomerId(), 
				service.getServiceType(), 
				service.getActivationDate(), 
				service.getExpirationDate(), 
				service.getAmount(), 
				service.getStatus());
		
    }
	
	private CustomerServiceEntity toCustomerServiceEntity(CustomerService service) {
		
		return new CustomerServiceEntity(
				service.getId(), 
				service.getCustomerId(), 
				service.getServiceType(), 
				service.getActivationDate(), 
				service.getExpirationDate(), 
				service.getAmount(), 
				service.getStatus());
		
    }

}
