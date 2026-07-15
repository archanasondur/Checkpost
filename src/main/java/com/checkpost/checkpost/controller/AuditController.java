package com.checkpost.checkpost.controller;

import com.checkpost.checkpost.model.AuditLog;
import com.checkpost.checkpost.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/audit")
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> list(@RequestParam(required = false) Long actionRequestId) {
        if (actionRequestId != null) {
            return ResponseEntity.ok(auditLogService.forActionRequest(actionRequestId));
        }
        return ResponseEntity.ok(auditLogService.all());
    }

    @GetMapping("/verify")
    public ResponseEntity<AuditLogService.VerificationResult> verify() {
        return ResponseEntity.ok(auditLogService.verifyChain());
    }
}