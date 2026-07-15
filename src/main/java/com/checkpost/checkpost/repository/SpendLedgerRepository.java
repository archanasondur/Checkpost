package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.SpendLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface SpendLedgerRepository extends JpaRepository<SpendLedger, Long> {
    Optional<SpendLedger> findByAgentIdAndDate(Long agentId, LocalDate date);
}