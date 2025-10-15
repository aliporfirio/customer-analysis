package com.aruba.customeranalysis.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aruba.customeranalysis.domain.job.NotificationJob;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    
    private final NotificationWorker worker;

    public NotificationDispatcher(NotificationWorker worker) {
        this.worker = worker;
    }

    public void dispatch(NotificationJob job) {
    	
        log.info("Dispatching notification job: {} for customer {}", job.type(), job.customerId());
        
        try {
            worker.process(job);
        } catch (Exception e) {
            log.error("Worker failed for job {}: {}", job, e.getMessage());
        }
        
    }
}
