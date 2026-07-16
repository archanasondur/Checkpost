package com.checkpost.checkpost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final RestClient restClient = RestClient.create();

    public void send(String callbackUrl, Long actionRequestId, String state, String detail) {
        if (callbackUrl == null || callbackUrl.isBlank()) return;

        try {
            restClient.post()
                .uri(callbackUrl)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "actionRequestId", actionRequestId,
                    "state", state,
                    "detail", detail == null ? "" : detail
                ))
                .retrieve()
                .toBodilessEntity();
            log.info("Webhook delivered to {} for action {}", callbackUrl, actionRequestId);
        } catch (Exception e) {
            log.warn("Webhook delivery failed for action {} to {}: {}", actionRequestId, callbackUrl, e.getMessage());
        }
    }
}