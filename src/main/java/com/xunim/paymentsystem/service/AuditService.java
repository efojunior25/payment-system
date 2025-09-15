package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {

    void logAction(String entityType, String entityId, String action, String details);

    void logAction(String entityType, String entityId, String action, Map<String, Object> details);

    List<AuditLog> getAuditLogsByEntity(String entityType, String entityId);

    List<AuditLog> getAuditLogsByUser(String userId);

    List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end);

    Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable);

    long countActionsSince(String action, LocalDateTime since);
}
