package com.checkpost.checkpost.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);

    @KafkaListener(topics = EventPublisherService.TOPIC, groupId = "checkpost")
    public void consume(String message) {
        log.info("Kafka event consumed: {}", message);
    }
}