package com.checkpost.checkpost.service;

import com.checkpost.checkpost.model.SpendLedger;
import com.checkpost.checkpost.repository.SpendLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class SpendLedgerService {

    @Autowired
    private SpendLedgerRepository repository;

    public boolean wouldExceed(Long agentId, Double cost, Double maxSpendPerDay) {
        if (maxSpendPerDay == null || cost == null) return false;
        double current = getTodaySpend(agentId);
        return (current + cost) > maxSpendPerDay;
    }

    public double getTodaySpend(Long agentId) {
        return repository.findByAgentIdAndDate(agentId, LocalDate.now())
            .map(SpendLedger::getAmountSpent)
            .orElse(0.0);
    }

    public void record(Long agentId, Double cost) {
        if (cost == null || cost == 0.0) return;
        SpendLedger ledger = repository.findByAgentIdAndDate(agentId, LocalDate.now())
            .orElseGet(() -> {
                SpendLedger l = new SpendLedger();
                l.setAgentId(agentId);
                l.setDate(LocalDate.now());
                l.setAmountSpent(0.0);
                return l;
            });
        ledger.setAmountSpent(ledger.getAmountSpent() + cost);
        repository.save(ledger);
    }
}