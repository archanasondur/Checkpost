package com.checkpost.checkpost.controller;

import com.checkpost.checkpost.dto.ActionRequestDto;
import com.checkpost.checkpost.dto.DecisionDto;
import com.checkpost.checkpost.model.ActionRequest;
import com.checkpost.checkpost.service.ActionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/actions")
public class ActionRequestController {

    @Autowired
    private ActionRequestService service;

    @PostMapping
    public ResponseEntity<ActionRequest> submit(@RequestBody ActionRequestDto dto) {
        ActionRequest result = service.submit(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionRequest> get(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/kill")
    public ResponseEntity<ActionRequest> kill(@PathVariable Long id) {
        return service.kill(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<java.util.List<ActionRequest>> pending() {
        return ResponseEntity.ok(service.findPending());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ActionRequest> approve(@PathVariable Long id, @RequestBody(required = false) DecisionDto dto) {
        String decidedBy = (dto != null && dto.getDecidedBy() != null) ? dto.getDecidedBy() : "dashboard-user";
        return service.approve(id, decidedBy)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deny")
    public ResponseEntity<ActionRequest> deny(@PathVariable Long id, @RequestBody(required = false) DecisionDto dto) {
        String decidedBy = (dto != null && dto.getDecidedBy() != null) ? dto.getDecidedBy() : "dashboard-user";
        String reason = (dto != null) ? dto.getReason() : null;
        return service.deny(id, decidedBy, reason)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}