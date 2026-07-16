package com.checkpost.checkpost;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

@Testcontainers
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckpostIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deleteActionRequiresApproval() throws Exception {
        // Create the policy
        Map<String, Object> policy = new HashMap<>();
        policy.put("toolPattern", "DELETE *");
        policy.put("action", "REQUIRE_APPROVAL");
        policy.put("riskTier", "HIGH");
        postJson("/v1/policies", policy);

        // Submit a DELETE action
        Map<String, Object> action = new HashMap<>();
        action.put("agentId", 1);
        action.put("toolName", "DELETE_RECORD");
        action.put("payload", "{}");

        ResponseEntity<String> response = postJson("/v1/actions", action);
        Map<?, ?> body = objectMapper.readValue(response.getBody(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.get("state")).isEqualTo("PENDING_APPROVAL");
        assertThat(body.get("riskTier")).isEqualTo("HIGH");
    }

    @Test
    void rateLimitDeniesAfterThreshold() throws Exception {
        Map<String, Object> policy = new HashMap<>();
        policy.put("toolPattern", "EMAIL");
        policy.put("action", "ALLOW");
        policy.put("riskTier", "LOW");
        policy.put("maxCallsPerMinute", 2);
        postJson("/v1/policies", policy);

        Map<String, Object> action = new HashMap<>();
        action.put("agentId", 99);
        action.put("toolName", "SEND_EMAIL");
        action.put("payload", "{}");

        postJson("/v1/actions", action);
        postJson("/v1/actions", action);
        ResponseEntity<String> thirdCall = postJson("/v1/actions", action);

        Map<?, ?> body = objectMapper.readValue(thirdCall.getBody(), Map.class);
        assertThat(body.get("state")).isEqualTo("DENIED");
    }

    @Test
    void tamperedAuditRowFailsVerification() throws Exception {
        Map<String, Object> action = new HashMap<>();
        action.put("agentId", 50);
        action.put("toolName", "SEND_EMAIL");
        action.put("payload", "{}");
        postJson("/v1/actions", action);

        ResponseEntity<String> verifyResponse = restTemplate.getForEntity("/v1/audit/verify", String.class);
        Map<?, ?> result = objectMapper.readValue(verifyResponse.getBody(), Map.class);
        assertThat(result.get("valid")).isEqualTo(true);
    }

    private ResponseEntity<String> postJson(String path, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(path, entity, String.class);
    }
}