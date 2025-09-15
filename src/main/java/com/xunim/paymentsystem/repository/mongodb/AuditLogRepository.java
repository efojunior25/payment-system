package com.xunim.paymentsystem.repository.mongodb;

import com.xunim.paymentsystem.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<AuditLog> findByUserId(String userId);

    List<AuditLog> findByAction(String action);

    @Query("{ 'timestamp' : { $gte: ?0, $lte: ?1 } }")
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    long countByActionAndTimestampGreaterThan(String action, LocalDateTime timestamp);

}
