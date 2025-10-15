package com.aruba.customeranalysis.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.aruba.customeranalysis.domain.event.CustomerEvent;

@Component
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Value("${spring.kafka.expired-topic}")
    private String expiredTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CustomerEvent event) {
    	
        logger.info("Publishing event: {} for customer {}", event.getNotificationType(), event.getCustomerId());
        kafkaTemplate.send(expiredTopic, event.getCustomerId(), event);
        
    }
}