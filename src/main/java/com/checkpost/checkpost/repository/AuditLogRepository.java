package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActionRequestIdOrderByTimestampAsc(Long actionRequestId);
}