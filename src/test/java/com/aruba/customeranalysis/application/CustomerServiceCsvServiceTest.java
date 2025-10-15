package com.aruba.customeranalysis.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.ServiceStatus;
import com.aruba.customeranalysis.domain.model.ServiceType;

@ExtendWith(MockitoExtension.class)
class CustomerServiceCsvServiceTest {

    @Mock
    private CustomerServiceRepositoryInterface repository;

    @InjectMocks
    private CustomerServiceCsvService service;

    private String validCsv;
    private String invalidCsv;

    @BeforeEach
    void setup() {
    	
        validCsv = String.join("\n",
                "customer_id,service_type,status,activation_date,expiration_date,amount",
                "CUST001,HOSTING,ACTIVE,2024-01-01,2025-12-31,100.50"
        );

        invalidCsv = String.join("\n",
                "customer_id,service_type,status,activation_date,expiration_date,amount",
                "CUST002,INVALID_TYPE,ACTIVE,2024-01-01,2025-12-31,200.00"
        );
    }

    @Test
    void processCsv_shouldParseAndSaveValidRecord() throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.csv",
                "text/csv",
                validCsv.getBytes()
        );

        service.processCsv(file);

        ArgumentCaptor<CustomerService> captor = ArgumentCaptor.forClass(CustomerService.class);
        verify(repository, times(1)).saveOrUpdate(captor.capture());

        CustomerService saved = captor.getValue();
        assertEquals("CUST001", saved.getCustomerId());
        assertEquals(ServiceType.HOSTING, saved.getServiceType());
        assertEquals(ServiceStatus.ACTIVE, saved.getStatus());
        assertEquals(LocalDate.of(2024, 1, 1), saved.getActivationDate());
        assertEquals(LocalDate.of(2025, 12, 31), saved.getExpirationDate());
        assertEquals(new BigDecimal("100.50"), saved.getAmount());
    }

    @Test
    void processCsv_shouldSkipInvalidRowsAndNotThrow() throws IOException {
     
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsv.getBytes()
        );

        assertDoesNotThrow(() -> service.processCsv(file));
        verify(repository, never()).saveOrUpdate(any());
    }

    @Test
    void processCsv_shouldHandleMultipleRows() throws IOException {
        
        String multiCsv = String.join("\n",
                "customer_id,service_type,status,activation_date,expiration_date,amount",
                "CUST001,HOSTING,ACTIVE,2024-01-01,2024-12-31,100.00",
                "CUST002,PEC,EXPIRED,2024-02-01,2024-12-31,50.00"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "multi.csv",
                "text/csv",
                multiCsv.getBytes()
        );

        service.processCsv(file);

        verify(repository, times(2)).saveOrUpdate(any(CustomerService.class));
    }
}
