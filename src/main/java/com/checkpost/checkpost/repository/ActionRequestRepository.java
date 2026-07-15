package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.ActionRequest;
import com.checkpost.checkpost.model.ActionState;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActionRequestRepository extends JpaRepository<ActionRequest, Long> {
    Optional<ActionRequest> findByIdempotencyKey(String idempotencyKey);
    java.util.List<ActionRequest> findByState(ActionState state);
}