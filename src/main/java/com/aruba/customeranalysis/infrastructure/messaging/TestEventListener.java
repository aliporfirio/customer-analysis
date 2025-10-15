package com.aruba.customeranalysis.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aruba.customeranalysis.domain.event.CustomerEvent;

@Component
public class TestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TestEventListener.class);

    @KafkaListener(topics = "${spring.kafka.expired-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCustomerEvent(CustomerEvent event) {

        logger.info("[Kafka] Received CustomerEvent: type={}, customerId={}, expiredCount={}",
        		event.getNotificationType(),
        		event.getCustomerId(),                
                event.getExpiredCount());

    }
}

