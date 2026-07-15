package com.checkpost.checkpost.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "action_requests")
public class ActionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON string

    @Column(name = "risk_tier", nullable = false)
    private String riskTier = "LOW";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionState state = ActionState.SUBMITTED;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decided_by")
    private String decidedBy;

    public ActionRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getRiskTier() { return riskTier; }
    public void setRiskTier(String riskTier) { this.riskTier = riskTier; }

    public ActionState getState() { return state; }
    public void setState(ActionState state) { this.state = state; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
}