package com.checkpost.checkpost.service;

import com.checkpost.checkpost.model.AuditLog;
import com.checkpost.checkpost.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repository;

    public synchronized AuditLog write(Long actionRequestId, String eventType, String detail) {
        String prevHash = repository.findTopByOrderByIdDesc()
            .map(AuditLog::getEntryHash)
            .orElse("GENESIS");

        Instant now = Instant.now();
        String entryHash = computeHash(prevHash, actionRequestId, eventType, detail, now);

        AuditLog log = new AuditLog();
        log.setActionRequestId(actionRequestId);
        log.setEventType(eventType);
        log.setDetail(detail);
        log.setPrevHash(prevHash);
        log.setEntryHash(entryHash);
        log.setTimestamp(now);

        return repository.save(log);
    }

    public List<AuditLog> forActionRequest(Long actionRequestId) {
        return repository.findByActionRequestIdOrderByTimestampAsc(actionRequestId);
    }

    public List<AuditLog> all() {
        return repository.findAllByOrderByIdAsc();
    }

    public VerificationResult verifyChain() {
        List<AuditLog> logs = repository.findAllByOrderByIdAsc();
        String expectedPrev = "GENESIS";

        for (AuditLog log : logs) {
            if (!expectedPrev.equals(log.getPrevHash())) {
                return new VerificationResult(false, log.getId(), "prevHash mismatch, chain broken before this entry");
            }
            String recomputed = computeHash(log.getPrevHash(), log.getActionRequestId(), log.getEventType(), log.getDetail(), log.getTimestamp());
            if (!recomputed.equals(log.getEntryHash())) {
                return new VerificationResult(false, log.getId(), "entryHash does not match recomputed hash, this row was altered");
            }
            expectedPrev = log.getEntryHash();
        }
        return new VerificationResult(true, null, "chain intact across " + logs.size() + " entries");
    }

    private String computeHash(String prevHash, Long actionRequestId, String eventType, String detail, Instant timestamp) {
        try {
            String raw = prevHash + "|" + actionRequestId + "|" + eventType + "|" + detail + "|" + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static class VerificationResult {
        public boolean valid;
        public Long brokenAtId;
        public String message;
        public VerificationResult(boolean valid, Long brokenAtId, String message) {
            this.valid = valid;
            this.brokenAtId = brokenAtId;
            this.message = message;
        }
    }
}