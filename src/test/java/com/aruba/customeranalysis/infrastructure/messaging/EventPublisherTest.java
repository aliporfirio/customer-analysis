package com.aruba.customeranalysis.infrastructure.messaging;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.aruba.customeranalysis.domain.event.CustomerEvent;
import com.aruba.customeranalysis.domain.model.NotificationType;


@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    private CustomerEvent event;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        eventPublisher = new EventPublisher(kafkaTemplate);
        
        try {
        	
			Field field = eventPublisher.getClass().getDeclaredField("expiredTopic");
			field.setAccessible(true);
            field.set(eventPublisher, "alerts.customer_expired");
		
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        event = new CustomerEvent(NotificationType.EXPIRED_ALERT, "CUST001", 4L);
        
    }

    @Test
    void shouldPublishEventToKafka() {

        eventPublisher.publish(event);

        verify(kafkaTemplate, times(1))
                .send("alerts.customer_expired", "CUST001", event);
    }

}
