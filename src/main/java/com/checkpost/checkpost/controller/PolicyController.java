package com.checkpost.checkpost.controller;

import com.checkpost.checkpost.model.Policy;
import com.checkpost.checkpost.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/policies")
public class PolicyController {

    @Autowired
    private PolicyRepository policyRepository;

    @PostMapping
    public ResponseEntity<Policy> create(@RequestBody Policy policy) {
        return ResponseEntity.ok(policyRepository.save(policy));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> list() {
        return ResponseEntity.ok(policyRepository.findAll());
    }
}