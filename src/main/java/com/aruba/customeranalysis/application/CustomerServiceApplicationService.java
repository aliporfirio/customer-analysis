package com.aruba.customeranalysis.application;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aruba.customeranalysis.domain.Constants;
import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.dto.CustomerAverageCostDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.CustomerExpiringServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.job.NotificationJob;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.NotificationType;
import com.aruba.customeranalysis.domain.model.ServiceStatus;
import com.aruba.customeranalysis.domain.model.ServiceType;

@Service
public class CustomerServiceApplicationService {

    private final CustomerServiceRepositoryInterface customerServiceRepositoryInterface;
    private final NotificationDispatcher notificationDispatcher;
    
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceApplicationService.class);
    
    public CustomerServiceApplicationService(CustomerServiceRepositoryInterface customerServiceRepositoryInterface,
    		NotificationDispatcher notificationDispatcher) {


			this.customerServiceRepositoryInterface = customerServiceRepositoryInterface;
			this.notificationDispatcher = notificationDispatcher;
			
	}
	
	public void processCsv(MultipartFile file) throws IOException {
		
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
	        
	    	CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
	    	        .setHeader()
	    	        .setSkipHeaderRecord(true)
	    	        .setIgnoreHeaderCase(true)
	    	        .setTrim(true)
	    	        .get();

	    	Iterable<CSVRecord> records = format.parse(reader);
	
	        for (CSVRecord record : records) {
	        	
	            try {
	            	
	            	CustomerService service = createFromCsvRecord(record);
	                customerServiceRepositoryInterface.save(service);
	                
	            } catch (IllegalArgumentException e) {
	                log.error("Invalid row: " + record.toString() + " - Errore: " + e.getMessage());
	            } catch (DataIntegrityViolationException e) {
	            	log.error("Duplicated row in database: " + record.toString());
	            }
	        }
	    }
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
	
	private CustomerService createFromCsvRecord(CSVRecord record) {
        
		CustomerService service = new CustomerService();

        String customerId = record.get("customer_id");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("Missing customer_id");
        service.setCustomerId(customerId);

        try {
            service.setServiceType(ServiceType.valueOf(record.get("service_type").toUpperCase()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid service_type");
        }

        try {
            service.setStatus(ServiceStatus.valueOf(record.get("status").toUpperCase()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid status");
        }

        try {
            service.setActivationDate(LocalDate.parse(record.get("activation_date")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid activation_date");
        }
        
        try {
            service.setExpirationDate(LocalDate.parse(record.get("expiration_date")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid expiration_date");
        }

        try {
            service.setAmount(new BigDecimal(record.get("amount")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid amount");
        }

        return service;
    }
	
    private void populateActiveServices(Sheet sheet, List<ServiceSummaryDTO> data) {
    	
        int rowId = 4;
        
        for (ServiceSummaryDTO item : data) {
        	
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getServiceType().name());            
            row.createCell(1).setCellValue(item.getActiveCount());
            
        }
    }

    private void populateAvgCost(Sheet sheet, List<CustomerAverageCostDTO> data) {
    	
        int rowId = 4;
        
        for (CustomerAverageCostDTO item : data) {
        	
            Row row = sheet.createRow(rowId++);
            row.createCell(0).setCellValue(item.getCustomerId());
            row.createCell(1).setCellValue(String.format("%.2f", item.getAverageCost()));
        
        }
    }

    private void populateExpiredServices(Sheet sheet, List<CustomerExpiredServiceDTO> data) {
        
    	int rowId = 4;
        
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
    	
        int rowId = 4;
        
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
