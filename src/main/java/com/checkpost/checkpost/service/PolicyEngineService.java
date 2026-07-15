package com.checkpost.checkpost.service;

import com.checkpost.checkpost.dto.PolicyDecision;
import com.checkpost.checkpost.model.Policy;
import com.checkpost.checkpost.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PolicyEngineService {

    @Autowired
    private PolicyRepository policyRepository;

    public PolicyDecision evaluate(Long agentId, String toolName, Double estimatedCost) {
        List<Policy> policies = policyRepository.findByAgentIdOrAgentIdIsNull(agentId);
        policies.sort((a, b) -> a.getId().compareTo(b.getId()));

        String tool = toolName == null ? "" : toolName.toUpperCase();

        for (Policy policy : policies) {
            if (!matchesPattern(tool, policy.getToolPattern())) continue;
            if (!matchesCondition(policy.getCondition(), estimatedCost)) continue;

            return new PolicyDecision(
                policy.getAction(),
                policy.getRiskTier(),
                policy,
                "Matched policy #" + policy.getId() + " (" + policy.getToolPattern() + ")"
            );
        }

        return new PolicyDecision("ALLOW", "LOW", null, "No matching policy, default allow");
    }

    private boolean matchesPattern(String tool, String pattern) {
        if (pattern == null) return false;
        String p = pattern.trim().toUpperCase();
        if (p.endsWith("*")) {
            String prefix = p.substring(0, p.length() - 1).trim();
            return tool.startsWith(prefix);
        }
        return tool.contains(p);
    }

    private boolean matchesCondition(String condition, Double estimatedCost) {
        if (condition == null || condition.isBlank()) return true;
        String[] parts = condition.trim().split("\\s+");
        if (parts.length != 3 || !parts[0].equalsIgnoreCase("cost")) return true;
        if (estimatedCost == null) return false;

        double threshold;
        try {
            threshold = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            return true;
        }

        return switch (parts[1]) {
            case ">" -> estimatedCost > threshold;
            case ">=" -> estimatedCost >= threshold;
            case "<" -> estimatedCost < threshold;
            case "<=" -> estimatedCost <= threshold;
            case "==" -> estimatedCost == threshold;
            default -> true;
        };
    }
}