package com.aruba.customeranalysis.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.aruba.customeranalysis.domain.EmailSenderInterface;
import com.aruba.customeranalysis.domain.model.ServiceType;

@Component
public class EmailSenderAdapter implements EmailSenderInterface {
	
	@Value("${notification.upsellmail.subject}")
	private String upsellingSubject;
		
	@Value("${notification.upsellmail.body}")
	private String upsellingBody;
	
	@Value("${notification.upsellmail.to}")
	private String upsellingToAddress;
   
    private final JavaMailSender mailSender;
    
    public EmailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendUpsellingEmail(String customerId, ServiceType serviceType) {
    	
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(upsellingToAddress);
        message.setSubject(String.format(upsellingSubject, customerId));
        message.setText(String.format(upsellingBody, customerId, serviceType.name()));

        mailSender.send(message);
        
    }
}
