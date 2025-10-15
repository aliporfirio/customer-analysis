package com.aruba.customeranalysis.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.aruba.customeranalysis.application.CustomerServiceCsvService;
import com.aruba.customeranalysis.application.CustomerServiceReportService;

@ExtendWith(MockitoExtension.class)
class CustomerSeviceControllerTest {

    @Mock
    private CustomerServiceCsvService customerServiceCsvService;

    @Mock
    private CustomerServiceReportService customerServiceReportService;

    @InjectMocks
    private CustomerSeviceController controller;

    @Test
    void uploadCsv_shouldCallServiceAndReturnOkResponse() throws IOException {
        
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "customers.csv", 
                "text/csv", 
                "customer_id,service_type,activation_date,expiration_date,amount,status".getBytes()
        );

        ResponseEntity<String> response = controller.uploadCsv(file);

        verify(customerServiceCsvService, times(1)).processCsv(file);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("CSV successfully uploaded and elaborated.", response.getBody());
    }

    @Test
    void getSummaryReport_shouldReturnExcelFileWithHeaders() throws IOException {
    
        byte[] fakeReport = "excel-data".getBytes();
        
        when(customerServiceReportService.generateExcelReport()).thenReturn(fakeReport);

        ResponseEntity<byte[]> response = controller.getSummaryReport();

        verify(customerServiceReportService, times(1)).generateExcelReport();
        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(fakeReport, response.getBody());

        String header = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(header);
        assertTrue(header.contains("attachment; filename="));
    }
}
