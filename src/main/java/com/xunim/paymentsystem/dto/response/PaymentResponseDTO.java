package com.xunim.paymentsystem.dto.response;

import com.xunim.paymentsystem.enums.PaymentStatus;
import com.xunim.paymentsystem.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String description;
    private String externalId;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}