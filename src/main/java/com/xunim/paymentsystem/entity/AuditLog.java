package com.xunim.paymentsystem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Field("entity_type")
    private String entityType;

    @Field("entity_id")
    private String entityId;

    private String action;

    @Field("user_id")
    private String userId;

    private LocalDateTime timestamp;

    private Map<String, Object> details;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    public enum Action {
        CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT
    }

    public enum EntityType {
        USER, ACCOUNT, PAYMENT, SYSTEM
    }
}
