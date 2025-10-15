package com.aruba.customeranalysis.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aruba.customeranalysis.domain.Constants;
import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.job.NotificationJob;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.NotificationType;

@Service
public class CustomerServiceReportService {
	
	private final CustomerServiceRepositoryInterface customerServiceRepositoryInterface;
    private final NotificationDispatcher notificationDispatcher;
    
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceReportService.class);
    
    public CustomerServiceReportService(CustomerServiceRepositoryInterface customerServiceRepositoryInterface,
    		NotificationDispatcher notificationDispatcher) {

		this.customerServiceRepositoryInterface = customerServiceRepositoryInterface;
		this.notificationDispatcher = notificationDispatcher;
			
	}
    
    public byte[] generateExcelReport() throws IOException {
		
        List<ServiceSummaryDTO> activeServices = customerServiceRepositoryInterface.findActiveServicesByType();
		List<CustomerAverageCostDTO> avgCosts = customerServiceRepositoryInterface.findAverageCostPerCustomer();
		List<CustomerExpiredServiceDTO> expiredServices = customerServiceRepositoryInterface.findCustomersWithExpiredServices();
		List<CustomerExpiringServiceDTO> expiringServices = customerServiceRepositoryInterface.findCustomersithExpiringServices(java.time.LocalDate.now().plusDays(15));

		InputStream template = getClass().getResourceAsStream(Constants.REPORT_TEMPLATE_PATH);		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try (Workbook workbook = new XSSFWorkbook(template)) {
			
			populateActiveServices(workbook.getSheet(Constants.ACTIVE_SERVICES_SHEET), activeServices);
			populateAvgCost(workbook.getSheet(Constants.AVG_COST_SHEET), avgCosts);
			populateExpiredServices(workbook.getSheet(Constants.EXPIRED_SERVICES_SHEET), expiredServices);
			populateExpiringServices(workbook.getSheet(Constants.EXPIRING_SERVICES_SHEET), expiringServices);
			
			workbook.write(out);
		}
		
		try {
			checkAndDispatchUpsellingNotifications();
		} catch (Exception e) {
			log.error("Error trying to check and dispatch upselling notifications", e);
		}
        
        return out.toByteArray();
    }
	
	
    private void populateActiveServices(Sheet sheet, List<ServiceSummaryDTO> data) {
    	
        int rowId = 3;
        
        for (ServiceSummaryDTO item : data) {
        	
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getServiceType().name());            
            row.createCell(1).setCellValue(item.getActiveCount());
            
        }
    }

    private void populateAvgCost(Sheet sheet, List<CustomerAverageCostDTO> data) {
    	
        int rowId = 3;
        
        for (CustomerAverageCostDTO item : data) {
        	
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getCustomerId());
            row.createCell(1).setCellValue(String.format("%.2f", item.getAverageCost()));
        
        }
    }

    private void populateExpiredServices(Sheet sheet, List<CustomerExpiredServiceDTO> data) {
        
    	int rowId = 3;
        
    	for (CustomerExpiredServiceDTO item : data) {
    		
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getCustomerId());
            row.createCell(1).setCellValue(item.getExpiredServiceCount());
            
            if (item.getExpiredServiceCount() > 5) {
            	
            	try {
            		
            		NotificationJob expiredNotificationJob = new NotificationJob(
            				NotificationType.EXPIRED_ALERT, 
            				item.getCustomerId(),
            				item.getExpiredServiceCount(),
            				null);
            		
            		notificationDispatcher.dispatch(expiredNotificationJob);
            		
            	} catch (Exception e) {
            		log.error("Error trying to dispatch expired notification", e);
            	}
            	
            }
            
        }
    }

    private void populateExpiringServices(Sheet sheet, List<CustomerExpiringServiceDTO> data) {
    	
        int rowId = 3;
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
        
        for (CustomerExpiringServiceDTO item : data) {
        	
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getCustomerId());
            row.createCell(1).setCellValue(item.getServiceType().name());
            row.createCell(2).setCellValue(item.getExpirationDate().format(fmt));
            
        }
    }
    
    private void checkAndDispatchUpsellingNotifications() {
    	
    	LocalDate limitDate = LocalDate.now().minusYears(3);
    	
        List<CustomerService> oldActiveServices = customerServiceRepositoryInterface.findActiveServicesOlderThan(limitDate);

        for (CustomerService service : oldActiveServices) {
        	
            NotificationJob job = new NotificationJob(
                NotificationType.UPSELL_EMAIL,
                service.getCustomerId(),
                null,
                service.getServiceType()
            );
            notificationDispatcher.dispatch(job);
        }
    }

}
