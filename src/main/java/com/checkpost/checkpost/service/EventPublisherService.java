package com.checkpost.checkpost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {

    public static final String TOPIC = "action-state-changes";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publish(Long actionRequestId, String eventType, String detail) {
        String message = "actionRequestId=" + actionRequestId + ", event=" + eventType + ", detail=" + detail;
        kafkaTemplate.send(TOPIC, actionRequestId.toString(), message);
    }
}