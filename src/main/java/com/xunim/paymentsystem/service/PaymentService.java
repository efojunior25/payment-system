package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.dto.request.CreatePaymentRequestDTO;
import com.xunim.paymentsystem.dto.response.PaymentResponseDTO;
import com.xunim.paymentsystem.enums.PaymentStatus;
import com.xunim.paymentsystem.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponseDTO createPayment(CreatePaymentRequestDTO request);

    PaymentResponseDTO getPaymentById(Long id);

    PaymentResponseDTO getPaymentByTransactionId(String transactionId);

    List<PaymentResponseDTO> getPaymentsByAccountId(Long accountId);

    Page<PaymentResponseDTO> getPaymentsByAccount(Long accountId, Pageable pageable);

    List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status);

    List<PaymentResponseDTO> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end);

    PaymentResponseDTO processPayment(Long paymentId);

    PaymentResponseDTO cancelPayment(Long paymentId);

    BigDecimal getTotalAmountByType(PaymentType type);

    long countPaymentsByStatusSince(PaymentStatus status, LocalDateTime since);
}