package com.aruba.customeranalysis.infrastructure.rest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aruba.customeranalysis.application.CustomerServiceCsvService;
import com.aruba.customeranalysis.application.CustomerServiceReportService;
import com.aruba.customeranalysis.domain.Constants;

@RestController
@RequestMapping("/api/customerservice")
public class CustomerSeviceController {
	
	private final CustomerServiceCsvService customerServiceCsvService;
	private final CustomerServiceReportService customerServiceReportService;
	
	private static final Logger log = LoggerFactory.getLogger(CustomerSeviceController.class);
	
	public CustomerSeviceController(CustomerServiceCsvService customerServiceCsvService,
			CustomerServiceReportService customerServiceReportService) {
		
        this.customerServiceCsvService = customerServiceCsvService;
        this.customerServiceReportService = customerServiceReportService;
    }
	
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCsv(@RequestParam(name = "file", required = true) MultipartFile file) 
    		throws IOException {
		
		log.info("Upload CSV: " + file.getOriginalFilename());
		
        customerServiceCsvService.processCsv(file);
        return ResponseEntity.ok("CSV successfully uploaded and elaborated.");
        
    }
	
	@GetMapping(value = "/summary", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<byte[]> getSummaryReport() throws IOException {
    	
    	log.info("Generate summary report");
    	
        byte[] file = customerServiceReportService.generateExcelReport();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT);        
        String filename = String.format(Constants.REPORT_FILENAME, dtf.format(OffsetDateTime.now()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+filename)
                .body(file);
    }

}
