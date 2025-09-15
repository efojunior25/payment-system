package com.xunim.paymentsystem.service.impl;

import com.xunim.paymentsystem.entity.AuditLog;
import com.xunim.paymentsystem.repository.mongodb.AuditLogRepository;
import com.xunim.paymentsystem.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(String entityType, String entityId, String action, String details) {
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("description", details);
        logAction(entityType, entityId, action, detailsMap);
    }

    @Override
    public void logAction(String entityType, String entityId, String action, Map<String, Object> details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Log de auditoria salvo: {} {} {}", entityType, entityId, action);
        } catch (Exception e) {
            log.error("Erro ao salvar log de auditoria", e);
        }
    }

    @Override
    public List<AuditLog> getAuditLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    public List<AuditLog> getAuditLogsByUser(String userId) {
        return auditLogRepository.findByUserId(userId);
    }

    @Override
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    @Override
    public Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    @Override
    public long countActionsSince(String action, LocalDateTime since) {
        return auditLogRepository.countByActionAndTimestampGreaterThan(action, since);
    }
}
