package com.aruba.customeranalysis.infrastructure.persistence;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.ServiceStatus;
import com.aruba.customeranalysis.domain.model.ServiceType;
import com.aruba.customeranalysis.infrastructure.persistence.model.CustomerServiceEntity;

@ExtendWith(MockitoExtension.class)
class CustomerServiceRepositoryAdapterTest {

    @Mock
    private JpaCustomerServiceRepository jpaRepository;

    @InjectMocks
    private CustomerServiceRepositoryAdapter adapter;

    @Test
    void save_shouldMapAndCallJpaRepository() {

    	UUID randomId = UUID.randomUUID();    	
    	
        CustomerService domainObj = new CustomerService(null, "CUST001", ServiceType.HOSTING,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                BigDecimal.valueOf(100), ServiceStatus.ACTIVE);

        CustomerServiceEntity savedEntity = new CustomerServiceEntity(randomId, domainObj.getCustomerId(), domainObj.getServiceType(),
        		domainObj.getActivationDate(), domainObj.getExpirationDate(),
        		domainObj.getAmount(), domainObj.getStatus());

        when(jpaRepository.save(any(CustomerServiceEntity.class))).thenReturn(savedEntity);

        CustomerService result = adapter.save(domainObj);

        ArgumentCaptor<CustomerServiceEntity> captor = ArgumentCaptor.forClass(CustomerServiceEntity.class);
        verify(jpaRepository).save(captor.capture());

        CustomerServiceEntity captured = captor.getValue();
        assertEquals(domainObj.getCustomerId(), captured.getCustomerId());
        assertEquals(domainObj.getServiceType(), captured.getServiceType());

        assertNotNull(result);
        assertEquals(randomId, result.getId());
        assertEquals(domainObj.getCustomerId(), result.getCustomerId());
    }

    @Test
    void saveOrUpdate_shouldUpdateExistingService() {

    	UUID randomId = UUID.randomUUID();  
    	
        CustomerService domain = new CustomerService(null, "CUST002", ServiceType.PEC,
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 12, 31),
                BigDecimal.valueOf(50), ServiceStatus.EXPIRED);

        CustomerServiceEntity existingEntity = new CustomerServiceEntity(randomId, domain.getCustomerId(), domain.getServiceType(),
                domain.getActivationDate(), LocalDate.of(2025, 11, 30),
                BigDecimal.valueOf(30), ServiceStatus.ACTIVE);

        when(jpaRepository.findByCustomerIdAndServiceTypeAndActivationDate(domain.getCustomerId(),
                domain.getServiceType(), domain.getActivationDate()))
                .thenReturn(Optional.of(existingEntity));

        when(jpaRepository.save(any(CustomerServiceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerService result = adapter.saveOrUpdate(domain);

        assertEquals(randomId, result.getId());
        assertEquals(BigDecimal.valueOf(50), result.getAmount());
        assertEquals(domain.getExpirationDate(), result.getExpirationDate());
        assertEquals(domain.getStatus(), result.getStatus());
        
        verify(jpaRepository).save(any(CustomerServiceEntity.class));
    }

    @Test
    void saveOrUpdate_shouldSaveNewServiceIfNotExists() {
        
    	UUID randomId = UUID.randomUUID(); 
    	
        CustomerService domain = new CustomerService(null, "CUST003", ServiceType.PEC,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 12, 31),
                BigDecimal.valueOf(70), ServiceStatus.ACTIVE);

        when(jpaRepository.findByCustomerIdAndServiceTypeAndActivationDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        when(jpaRepository.save(any(CustomerServiceEntity.class)))
                .thenAnswer(invocation -> new CustomerServiceEntity(randomId,
                        domain.getCustomerId(), domain.getServiceType(),
                        domain.getActivationDate(), domain.getExpirationDate(),
                        domain.getAmount(), domain.getStatus()));

        
        CustomerService result = adapter.saveOrUpdate(domain);

        assertEquals(randomId, result.getId());
        verify(jpaRepository).save(any(CustomerServiceEntity.class));
    }

    @Test
    void findServicesByCustomerIdAndServiceTypeAndActivationDate_shouldReturnServiceIfPresent() {
        
    	UUID randomId = UUID.randomUUID(); 
    	
        CustomerServiceEntity entity = new CustomerServiceEntity(randomId, "CUST004", ServiceType.HOSTING,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 12, 31),
                BigDecimal.valueOf(80), ServiceStatus.ACTIVE);

        when(jpaRepository.findByCustomerIdAndServiceTypeAndActivationDate("CUST004", ServiceType.HOSTING,
                LocalDate.of(2024, 4, 1))).thenReturn(Optional.of(entity));

       
        CustomerService result = adapter.findServicesByCustomerIdAndServiceTypeAndActivationDate("CUST004",
                ServiceType.HOSTING, LocalDate.of(2024, 4, 1));

        
        assertNotNull(result);
        assertEquals(randomId, result.getId());
    }

    @Test
    void findServicesByCustomerIdAndServiceTypeAndActivationDate_shouldReturnNullIfNotPresent() {
        
        when(jpaRepository.findByCustomerIdAndServiceTypeAndActivationDate(any(), any(), any()))
                .thenReturn(Optional.empty());
        
        CustomerService result = adapter.findServicesByCustomerIdAndServiceTypeAndActivationDate("CUST000",
                ServiceType.PEC, LocalDate.now());
        
        assertNull(result);
    }

    @Test
    void delegatingMethods_shouldCallJpaRepository() {

    	UUID randomId = UUID.randomUUID(); 
    	
        List<ServiceSummaryDTO> summaryList = List.of(new ServiceSummaryDTO(ServiceType.HOSTING, 5L));
        List<CustomerAverageCostDTO> avgList = List.of(new CustomerAverageCostDTO("CUST001", 100.0));
        List<CustomerExpiredServiceDTO> expiredList = List.of(new CustomerExpiredServiceDTO("CUST002", 2L));
        List<CustomerExpiringServiceDTO> expiringList = List.of(new CustomerExpiringServiceDTO("CUST003", ServiceType.PEC, LocalDate.now()));

        when(jpaRepository.findActiveServicesByType()).thenReturn(summaryList);
        when(jpaRepository.findAverageCostPerCustomer()).thenReturn(avgList);
        when(jpaRepository.findCustomersWithMultipleExpired()).thenReturn(expiredList);
        when(jpaRepository.findCustomersExpiringSoon(any())).thenReturn(expiringList);
        
        when(jpaRepository.findActiveServicesOlderThan(any())).thenReturn(List.of(
                new CustomerServiceEntity(randomId, "CUST009", ServiceType.PEC, LocalDate.now().minusYears(5),
                        LocalDate.now().plusDays(10), BigDecimal.TEN, ServiceStatus.ACTIVE)
        ));

        assertEquals(summaryList, adapter.findActiveServicesByType());
        assertEquals(avgList, adapter.findAverageCostPerCustomer());
        assertEquals(expiredList, adapter.findCustomersWithExpiredServices());
        assertEquals(expiringList, adapter.findCustomersWithExpiringServices(LocalDate.now()));
        assertEquals(1, adapter.findActiveServicesOlderThan(LocalDate.now()).size());

        verify(jpaRepository).findActiveServicesByType();
        verify(jpaRepository).findAverageCostPerCustomer();
        verify(jpaRepository).findCustomersWithMultipleExpired();
        verify(jpaRepository).findCustomersExpiringSoon(any());
        verify(jpaRepository).findActiveServicesOlderThan(any());
    }
}
