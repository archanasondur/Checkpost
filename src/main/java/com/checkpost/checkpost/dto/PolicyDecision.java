package com.checkpost.checkpost.dto;

import com.checkpost.checkpost.model.Policy;

public class PolicyDecision {
    private String action;
    private String riskTier;
    private Policy matchedPolicy;
    private String reason;

    public PolicyDecision(String action, String riskTier, Policy matchedPolicy, String reason) {
        this.action = action;
        this.riskTier = riskTier;
        this.matchedPolicy = matchedPolicy;
        this.reason = reason;
    }

    public String getAction() { return action; }
    public String getRiskTier() { return riskTier; }
    public Policy getMatchedPolicy() { return matchedPolicy; }
    public String getReason() { return reason; }
}