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

    public ActionRequest submit(ActionRequestDto dto) {
        // Idempotency check: same key, same request, return the original
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

        // Hardcoded rule for Part 2. Real policy engine arrives in Part 4.
        String tool = dto.getToolName() == null ? "" : dto.getToolName().toUpperCase();
        if (tool.contains("DELETE")) {
            request.setRiskTier("HIGH");
            request.setState(ActionState.PENDING_APPROVAL);
        } else {
            request.setRiskTier("LOW");
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
}