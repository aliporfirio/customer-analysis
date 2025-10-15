package com.aruba.customeranalysis.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aruba.customeranalysis.domain.Constants;
import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.domain.model.ServiceType;

@ExtendWith(MockitoExtension.class)
class CustomerServiceReportServiceTest {

    @Mock
    private CustomerServiceRepositoryInterface repository;

    @Mock
    private NotificationDispatcher dispatcher;

    @Spy
    @InjectMocks
    private CustomerServiceReportService reportService;

    private byte[] emptyExcelTemplate;

    @BeforeEach
    void setup() throws IOException {
       
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
        	
            workbook.createSheet(Constants.ACTIVE_SERVICES_SHEET);
            workbook.createSheet(Constants.AVG_COST_SHEET);
            workbook.createSheet(Constants.EXPIRED_SERVICES_SHEET);
            workbook.createSheet(Constants.EXPIRING_SERVICES_SHEET);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            emptyExcelTemplate = baos.toByteArray();
            
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(emptyExcelTemplate);
        doReturn(new ByteArrayInputStream(emptyExcelTemplate))
        .when(reportService).getTemplateStream();
    }

    @Test
    void generateExcelReport_shouldCallAllRepositoryMethodsAndReturnWorkbook() throws Exception {
        when(repository.findActiveServicesByType()).thenReturn(
                List.of(new ServiceSummaryDTO(ServiceType.HOSTING, 10L))
        );
        when(repository.findAverageCostPerCustomer()).thenReturn(
                List.of(new CustomerAverageCostDTO("CUST001", 120.5))
        );
        when(repository.findCustomersWithExpiredServices()).thenReturn(
                List.of(new CustomerExpiredServiceDTO("CUST002", 3L))
        );
        when(repository.findCustomersWithExpiringServices(any())).thenReturn(
                List.of(new CustomerExpiringServiceDTO("CUST003", ServiceType.PEC, LocalDate.now().plusDays(10)))
        );
        when(repository.findActiveServicesOlderThan(any())).thenReturn(List.of());

        byte[] result = reportService.generateExcelReport();

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertDoesNotThrow(() -> WorkbookFactory.create(new ByteArrayInputStream(result)));

        verify(repository).findActiveServicesByType();
        verify(repository).findAverageCostPerCustomer();
        verify(repository).findCustomersWithExpiredServices();
        verify(repository).findCustomersWithExpiringServices(any());
        verify(repository).findActiveServicesOlderThan(any());
    }

    @Test
    void populateExpiredServices_shouldDispatchExpiredAlertWhenThresholdExceeded() throws Exception {
        CustomerExpiredServiceDTO expired = new CustomerExpiredServiceDTO("CUST999", 7L);

        when(repository.findActiveServicesByType()).thenReturn(List.of());
        when(repository.findAverageCostPerCustomer()).thenReturn(List.of());
        when(repository.findCustomersWithExpiredServices()).thenReturn(List.of(expired));
        when(repository.findCustomersWithExpiringServices(any())).thenReturn(List.of());
        when(repository.findActiveServicesOlderThan(any())).thenReturn(List.of());

        reportService.generateExcelReport();

        verify(dispatcher, atLeastOnce()).dispatch(argThat(job ->
                job.type() == NotificationType.EXPIRED_ALERT &&
                        "CUST999".equals(job.customerId())
        ));
    }

    @Test
    void checkAndDispatchUpsellingNotifications_shouldSendUpsellEmailsForOldServices() throws Exception {
        CustomerService old = new CustomerService();
        old.setCustomerId("CUST777");
        old.setServiceType(ServiceType.FATTURAZIONE);

        when(repository.findActiveServicesByType()).thenReturn(List.of());
        when(repository.findAverageCostPerCustomer()).thenReturn(List.of());
        when(repository.findCustomersWithExpiredServices()).thenReturn(List.of());
        when(repository.findCustomersWithExpiringServices(any())).thenReturn(List.of());
        when(repository.findActiveServicesOlderThan(any())).thenReturn(List.of(old));

        reportService.generateExcelReport();

        verify(dispatcher, atLeastOnce()).dispatch(argThat(job ->
                job.type() == NotificationType.UPSELL_EMAIL &&
                        "CUST777".equals(job.customerId())
        ));
    }

}
