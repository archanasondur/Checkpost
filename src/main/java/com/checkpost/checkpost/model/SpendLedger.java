package com.checkpost.checkpost.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "spend_ledger", uniqueConstraints = @UniqueConstraint(columnNames = {"agent_id", "date"}))
public class SpendLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "amount_spent", nullable = false)
    private Double amountSpent = 0.0;

    @Column(nullable = false)
    private String currency = "USD";

    public SpendLedger() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getAmountSpent() { return amountSpent; }
    public void setAmountSpent(Double amountSpent) { this.amountSpent = amountSpent; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}