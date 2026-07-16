package com.checkpost.checkpost.service;

import com.checkpost.checkpost.model.ActionRequest;
import com.checkpost.checkpost.repository.ActionRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);

    @Autowired
    private ActionRequestRepository actionRequestRepository;

    @Autowired
    private WebhookService webhookService;

    @KafkaListener(topics = EventPublisherService.TOPIC, groupId = "checkpost")
    public void consume(String message) {
        log.info("Kafka event consumed: {}", message);

        Long actionRequestId = extractActionRequestId(message);
        String eventType = extractEventType(message);
        if (actionRequestId == null || eventType == null) return;

        // Only fire webhooks for terminal decisions, not every intermediate event
        if (!eventType.equals("APPROVED") && !eventType.equals("DENIED") && !eventType.equals("KILLED")) {
            return;
        }

        Optional<ActionRequest> found = actionRequestRepository.findById(actionRequestId);
        if (found.isEmpty()) return;

        ActionRequest request = found.get();
        webhookService.send(request.getCallbackUrl(), actionRequestId, eventType, request.getDecisionReason());
    }

    private Long extractActionRequestId(String message) {
        try {
            String marker = "actionRequestId=";
            int start = message.indexOf(marker) + marker.length();
            int end = message.indexOf(",", start);
            return Long.parseLong(message.substring(start, end));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractEventType(String message) {
        try {
            String marker = "event=";
            int start = message.indexOf(marker) + marker.length();
            int end = message.indexOf(",", start);
            if (end == -1) end = message.length();
            return message.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}