package com.aruba.customeranalysis.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.model.CustomerService;
import com.aruba.customeranalysis.domain.model.ServiceStatus;
import com.aruba.customeranalysis.domain.model.ServiceType;

@Service
public class CustomerServiceCsvService {
	
	private final CustomerServiceRepositoryInterface customerServiceRepositoryInterface;
    
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceCsvService.class);
    
    public CustomerServiceCsvService(CustomerServiceRepositoryInterface customerServiceRepositoryInterface) {
		this.customerServiceRepositoryInterface = customerServiceRepositoryInterface;			
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
	        	
	        	CustomerService service = null;
	        	
	            try {
	            	
	            	service = createFromCsvRecord(record);
	                customerServiceRepositoryInterface.saveOrUpdate(service);
	                
	            } catch (IllegalArgumentException e) {
	                log.error("Invalid row: " + record.toString() + " - " + e.getMessage());
	            } catch (Exception e) {
	            	log.error("Error saving row: " + record.toString() + " - " + e.getMessage());
	            }
	        }
	    }
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
        
        if ((service.getStatus().equals(ServiceStatus.EXPIRED) && service.getExpirationDate().isAfter(LocalDate.now()))
        		|| (service.getStatus().equals(ServiceStatus.ACTIVE) && service.getExpirationDate().isBefore(LocalDate.now()))) {
        	throw new IllegalArgumentException("Invalid combination of service status and expiration date");        	        	
        }

        return service;
    }

}
