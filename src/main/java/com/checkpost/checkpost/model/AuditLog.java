package com.checkpost.checkpost.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_request_id", nullable = false)
    private Long actionRequestId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "prev_hash")
    private String prevHash;

    @Column(name = "entry_hash", nullable = false)
    private String entryHash;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActionRequestId() { return actionRequestId; }
    public void setActionRequestId(Long actionRequestId) { this.actionRequestId = actionRequestId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public String getEntryHash() { return entryHash; }
    public void setEntryHash(String entryHash) { this.entryHash = entryHash; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}