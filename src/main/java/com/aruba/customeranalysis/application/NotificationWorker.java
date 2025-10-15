package com.aruba.customeranalysis.application;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aruba.customeranalysis.domain.EmailSenderInterface;
import com.aruba.customeranalysis.domain.NotificationRepositoryInterface;
import com.aruba.customeranalysis.domain.event.CustomerEvent;
import com.aruba.customeranalysis.domain.job.NotificationJob;
import com.aruba.customeranalysis.domain.model.Notification;
import com.aruba.customeranalysis.domain.model.NotificationStatus;
import com.aruba.customeranalysis.infrastructure.messaging.EventPublisher;

@Service
public class NotificationWorker {

    @Value("${notification.expiredmail.max-retries:5}")
	private Integer expiredMaxRetries;
		
    @Value("${notification.upsellmail.max-retries:3}")
	private Integer upsellingMaxRetries;
    
    private final NotificationRepositoryInterface notificationRepository;
    private final EventPublisher eventPublisher;
    private final EmailSenderInterface emailSender;

    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);
    
    public NotificationWorker(NotificationRepositoryInterface notificationRepository, 
    		EventPublisher eventPublisher, EmailSenderInterface emailSender) {
    	
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
        this.emailSender = emailSender;
        
    }

    @Async
    public void process(NotificationJob job) {
       
    	log.info("Worker started for notification {}", job);

        Notification notification = new Notification();
        notification.setCustomerId(job.customerId());
        notification.setType(job.type());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedDatetime(OffsetDateTime.now());
        notification.setRetryCount(1);
        notificationRepository.save(notification);

        boolean sent = false;
        
        switch (job.type()) {
        
			case EXPIRED_ALERT: {
				
				CustomerEvent eventToSend = new CustomerEvent(job.type(), job.customerId(), job.expiredCount());
				
				for (int attempt = 1; attempt <= expiredMaxRetries && !sent; attempt++) {
					
		            try {
		            	
		            	eventPublisher.publish(eventToSend);
		            	notification.setStatus(NotificationStatus.SENT);
		                sent = true;
		                
		            } catch (Exception ex) {
		            	
		                log.error("Attemps {} failed in sending notification {}: {}", attempt, job.type(), ex.getMessage());
		               
		                notification.setRetryCount(attempt);
		                notification.setStatus(NotificationStatus.FAILED);
		                
		                sleep(attempt);
		                
		            } finally {
		            	
		            	notification.setUpdatedDatetime(OffsetDateTime.now());
		            	notificationRepository.save(notification);
		            }
		        }
	
		        if (sent) {
		            log.info("Notification {} related to customer {} successfully sent", job.type(), job.customerId());
		        } else {
		            log.error("Notification {} related to customer {} permanently failed after retries", job.type(), job.customerId());
		        }
				
				break;
			}
			
			case UPSELL_EMAIL: {
				
		        for (int attempt = 1; attempt <= upsellingMaxRetries && !sent; attempt++) {
		        	
		            try {
		            	
		            	emailSender.sendUpsellingEmail(job.customerId(), job.serviceType());
		                notification.setStatus(NotificationStatus.SENT);
		                sent = true;
		                
		            } catch (Exception ex) {
		            	
		                log.error("Attempt {} failed in sending notification {}: {}", attempt, job.type(), ex.getMessage());
		                
		                notification.setRetryCount(attempt);
		                notification.setStatus(NotificationStatus.FAILED);
		                
		                sleep(attempt);
		                
		            } finally {
		            	
		                notification.setUpdatedDatetime(OffsetDateTime.now());
		                notificationRepository.save(notification);
		                
		            }
		        }
		        

		        if (sent) {
		            log.info("Notification {} related to customer {} and service {} successfully sent", job.type(), job.customerId(), job.serviceType());
		        } else {
		            log.error("Notification {} related to customer {} and service {} permanently failed after retries", job.type(), job.customerId(), job.serviceType());
		        }
				
				
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + job.type());
		}

        
    }

    private void sleep(int attempt) {
       
    	try {
            Thread.sleep(attempt * 2000L);
        } catch (InterruptedException exception) {}
    	
    }

}
