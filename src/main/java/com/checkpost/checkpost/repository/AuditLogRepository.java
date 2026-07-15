package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActionRequestIdOrderByTimestampAsc(Long actionRequestId);
    Optional<AuditLog> findTopByOrderByIdDesc();
    java.util.List<AuditLog> findAllByOrderByIdAsc();
}