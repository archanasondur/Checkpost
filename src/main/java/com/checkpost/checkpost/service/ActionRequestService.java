package com.checkpost.checkpost.service;

import com.checkpost.checkpost.dto.ActionRequestDto;
import com.checkpost.checkpost.model.ActionRequest;
import com.checkpost.checkpost.model.ActionState;
import com.checkpost.checkpost.repository.ActionRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ActionRequestService {

    @Autowired
    private ActionRequestRepository repository;

    @Autowired
    private PolicyEngineService policyEngineService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private SpendLedgerService spendLedgerService;

    public ActionRequest submit(ActionRequestDto dto) {
        if (dto.getIdempotencyKey() != null) {
            Optional<ActionRequest> existing = repository.findByIdempotencyKey(dto.getIdempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        ActionRequest request = new ActionRequest();
        request.setAgentId(dto.getAgentId());
        request.setToolName(dto.getToolName());
        request.setPayload(dto.getPayload());
        request.setIdempotencyKey(dto.getIdempotencyKey());
        request.setState(ActionState.POLICY_EVALUATING);

        com.checkpost.checkpost.dto.PolicyDecision decision =
            policyEngineService.evaluate(dto.getAgentId(), dto.getToolName(), dto.getEstimatedCost());
        request.setRiskTier(decision.getRiskTier());

        if ("DENY".equalsIgnoreCase(decision.getAction())) {
            request.setState(ActionState.DENIED);
            request.setDecisionReason(decision.getReason());
            return repository.save(request);
        }

        com.checkpost.checkpost.model.Policy matched = decision.getMatchedPolicy();

        if (matched != null && rateLimiterService.exceedsLimit(dto.getAgentId(), matched.getMaxCallsPerMinute())) {
            request.setState(ActionState.DENIED);
            request.setDecisionReason("Rate limit exceeded (" + matched.getMaxCallsPerMinute() + "/min)");
            return repository.save(request);
        }

        if (matched != null && spendLedgerService.wouldExceed(dto.getAgentId(), dto.getEstimatedCost(), matched.getMaxSpendPerDay())) {
            request.setState(ActionState.DENIED);
            request.setDecisionReason("Daily spend cap exceeded ($" + matched.getMaxSpendPerDay() + ")");
            return repository.save(request);
        }

        if (dto.getEstimatedCost() != null) {
            spendLedgerService.record(dto.getAgentId(), dto.getEstimatedCost());
        }

        if ("REQUIRE_APPROVAL".equalsIgnoreCase(decision.getAction())) {
            request.setState(ActionState.PENDING_APPROVAL);
        } else {
            request.setState(ActionState.APPROVED);
        }

        return repository.save(request);
    }

    public Optional<ActionRequest> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<ActionRequest> kill(Long id) {
        Optional<ActionRequest> found = repository.findById(id);
        if (found.isEmpty()) return Optional.empty();

        ActionRequest request = found.get();
        ActionState state = request.getState();

        if (state == ActionState.EXECUTING || state == ActionState.APPROVED || state == ActionState.PENDING_APPROVAL) {
            request.setState(ActionState.KILLED);
            repository.save(request);
        }
        return Optional.of(request);
    }

    public java.util.List<ActionRequest> findPending() {
        return repository.findByState(ActionState.PENDING_APPROVAL);
    }

    public Optional<ActionRequest> approve(Long id, String decidedBy) {
        Optional<ActionRequest> found = repository.findById(id);
        if (found.isEmpty()) return Optional.empty();

        ActionRequest request = found.get();
        if (request.getState() != ActionState.PENDING_APPROVAL) {
            return Optional.of(request); // no-op if not awaiting approval
        }
        request.setState(ActionState.APPROVED);
        request.setDecidedBy(decidedBy);
        request.setDecidedAt(java.time.Instant.now());
        return Optional.of(repository.save(request));
    }

    public Optional<ActionRequest> deny(Long id, String decidedBy, String reason) {
        Optional<ActionRequest> found = repository.findById(id);
        if (found.isEmpty()) return Optional.empty();

        ActionRequest request = found.get();
        if (request.getState() != ActionState.PENDING_APPROVAL) {
            return Optional.of(request);
        }
        request.setState(ActionState.DENIED);
        request.setDecidedBy(decidedBy);
        request.setDecidedAt(java.time.Instant.now());
        request.setDecisionReason(reason);
        return Optional.of(repository.save(request));
    }
}