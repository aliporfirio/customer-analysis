package com.aruba.customeranalysis.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.aruba.customeranalysis.domain.model.ServiceType;

@ExtendWith(MockitoExtension.class)
class EmailSenderAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderAdapter emailSenderAdapter;
        
    private String subject;
    private String body;
    private String mail;
    

    @BeforeEach
    void setup() {

    	subject = "Subject";
        body = "Body";
        mail = "mail@foo.it";
    	
        try {
        	
			Field fieldSubject = emailSenderAdapter.getClass().getDeclaredField("upsellingSubject");
			fieldSubject.setAccessible(true);
			fieldSubject.set(emailSenderAdapter, subject);
			
			Field fieldBody = emailSenderAdapter.getClass().getDeclaredField("upsellingBody");
			fieldBody.setAccessible(true);
			fieldBody.set(emailSenderAdapter, body);
			
			Field fieldTo = emailSenderAdapter.getClass().getDeclaredField("upsellingToAddress");
			fieldTo.setAccessible(true);
			fieldTo.set(emailSenderAdapter, mail);
		
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
    }

    @Test
    void sendUpsellingEmail_shouldSendMailWithCorrectFields() {
        
    	ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSenderAdapter.sendUpsellingEmail("CUST123", ServiceType.HOSTING);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();

        assertThat(msg.getTo()).containsExactly(mail);
        assertThat(msg.getSubject()).isEqualTo(subject);
        assertThat(msg.getText()).contains(body);
        
    }

}
