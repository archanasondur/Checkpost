package com.checkpost.checkpost.controller;

import com.checkpost.checkpost.service.SpendLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/v1/agents")
public class AgentController {

    @Autowired
    private SpendLedgerService spendLedgerService;

    @GetMapping("/{id}/spend")
    public ResponseEntity<Map<String, Object>> spend(@PathVariable Long id) {
        double spent = spendLedgerService.getTodaySpend(id);
        return ResponseEntity.ok(Map.of("agentId", id, "spentToday", spent));
    }
}