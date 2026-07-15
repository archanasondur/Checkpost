package com.checkpost.checkpost.model;

import jakarta.persistence.*;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id")
    private Long agentId; // nullable = applies globally to all agents

    @Column(name = "tool_pattern", nullable = false)
    private String toolPattern; // e.g. "DELETE *", "EMAIL", "payments"

    @Column
    private String condition; // e.g. "cost > 100", null = no condition

    @Column(nullable = false)
    private String action; // "ALLOW", "REQUIRE_APPROVAL", "DENY"

    @Column(name = "max_calls_per_minute")
    private Integer maxCallsPerMinute;

    @Column(name = "max_spend_per_day")
    private Double maxSpendPerDay;

    @Column(name = "risk_tier", nullable = false)
    private String riskTier = "LOW"; // LOW, MEDIUM, HIGH

    public Policy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getToolPattern() { return toolPattern; }
    public void setToolPattern(String toolPattern) { this.toolPattern = toolPattern; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Integer getMaxCallsPerMinute() { return maxCallsPerMinute; }
    public void setMaxCallsPerMinute(Integer maxCallsPerMinute) { this.maxCallsPerMinute = maxCallsPerMinute; }

    public Double getMaxSpendPerDay() { return maxSpendPerDay; }
    public void setMaxSpendPerDay(Double maxSpendPerDay) { this.maxSpendPerDay = maxSpendPerDay; }

    public String getRiskTier() { return riskTier; }
    public void setRiskTier(String riskTier) { this.riskTier = riskTier; }
}