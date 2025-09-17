package com.xunim.paymentsystem.service.impl;

import com.xunim.paymentsystem.dto.request.CreatePaymentRequestDTO;
import com.xunim.paymentsystem.dto.response.PaymentResponseDTO;
import com.xunim.paymentsystem.entity.Account;
import com.xunim.paymentsystem.entity.Payment;
import com.xunim.paymentsystem.enums.AccountStatus;
import com.xunim.paymentsystem.enums.PaymentStatus;
import com.xunim.paymentsystem.enums.PaymentType;
import com.xunim.paymentsystem.exception.BusinessException;
import com.xunim.paymentsystem.exception.ResourceNotFoundException;
import com.xunim.paymentsystem.repository.mysql.AccountRepository;
import com.xunim.paymentsystem.repository.mysql.PaymentRepository;
import com.xunim.paymentsystem.service.AccountService;
import com.xunim.paymentsystem.service.AuditService;
import com.xunim.paymentsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AuditService auditService;

    @Override
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO request) {
        log.info("Criando pagamento: {} -> {}, valor: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // Validações
        validatePaymentRequest(request);

        // Buscar contas
        Account fromAccount = null;
        if (request.getFromAccountId() != null) {
            fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta origem não encontrada: " + request.getFromAccountId()));

            if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException("Conta origem não está ativa");
            }
        }

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta destino não encontrada: " + request.getToAccountId()));

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Conta destino não está ativa");
        }

        // Validar saldo (para transferências e débitos)
        if (fromAccount != null && needsBalanceCheck(request.getPaymentType())) {
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new BusinessException("Saldo insuficiente");
            }
        }

        // Criar pagamento
        Payment payment = Payment.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .externalId(request.getExternalId())
                .transactionId(generateTransactionId())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        auditService.logAction("PAYMENT", savedPayment.getId().toString(), "CREATE",
                String.format("Pagamento criado: %s, valor: %s", savedPayment.getTransactionId(), savedPayment.getAmount()));

        log.info("Pagamento criado com sucesso: ID {}", savedPayment.getId());
        return convertToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        log.debug("Buscando pagamento por ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + id));

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByTransactionId(String transactionId) {
        log.debug("Buscando pagamento por transaction ID: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + transactionId));

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByAccountId(Long accountId) {
        log.debug("Buscando pagamentos da conta ID: {}", accountId);

        List<Payment> sentPayments = paymentRepository.findByFromAccountId(accountId);
        List<Payment> receivedPayments = paymentRepository.findByToAccountId(accountId);

        sentPayments.addAll(receivedPayments);

        return sentPayments.stream()
                .distinct()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByAccount(Long accountId, Pageable pageable) {
        log.debug("Buscando pagamentos paginados da conta ID: {}", accountId);

        return paymentRepository.findByAccountId(accountId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        log.debug("Buscando pagamentos por status: {}", status);

        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Buscando pagamentos por período: {} - {}", start, end);

        return paymentRepository.findByDateRange(start, end)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO processPayment(Long paymentId) {
        log.info("Processando pagamento ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Apenas pagamentos pendentes podem ser processados");
        }

        try {
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Processar transferência de saldo
            boolean success = processBalanceTransfer(payment);

            if (success) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setProcessedAt(LocalDateTime.now());

                auditService.logAction("PAYMENT", payment.getId().toString(), "UPDATE",
                        "Pagamento processado com sucesso: " + payment.getTransactionId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);

                auditService.logAction("PAYMENT", payment.getId().toString(), "UPDATE",
                        "Falha no processamento do pagamento: " + payment.getTransactionId());
            }

            Payment updatedPayment = paymentRepository.save(payment);
            log.info("Pagamento processado: ID {}, Status: {}", paymentId, updatedPayment.getStatus());

            return convertToResponse(updatedPayment);

        } catch (Exception e) {
            log.error("Erro ao processar pagamento ID: {}", paymentId, e);

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            throw new BusinessException("Erro no processamento do pagamento: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO cancelPayment(Long paymentId) {
        log.info("Cancelando pagamento ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BusinessException("Pagamentos concluídos não podem ser cancelados");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Pagamento já está cancelado");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment updatedPayment = paymentRepository.save(payment);

        auditService.logAction("PAYMENT", payment.getId().toString(), "UPDATE",
                "Pagamento cancelado: " + payment.getTransactionId());

        log.info("Pagamento cancelado com sucesso: ID {}", paymentId);
        return convertToResponse(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByType(PaymentType type) {
        BigDecimal total = paymentRepository.getTotalAmountByType(type);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long countPaymentsByStatusSince(PaymentStatus status, LocalDateTime since) {
        return paymentRepository.countByStatusSince(status, since);
    }

    private void validatePaymentRequest(CreatePaymentRequestDTO request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor deve ser maior que zero");
        }

        if (request.getFromAccountId() != null &&
                request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("Conta origem e destino não podem ser iguais");
        }

        // PIX e boleto podem não ter conta origem
        if (needsFromAccount(request.getPaymentType()) && request.getFromAccountId() == null) {
            throw new BusinessException("Tipo de pagamento requer conta origem");
        }
    }

    private boolean needsFromAccount(PaymentType paymentType) {
        return paymentType == PaymentType.TRANSFER || paymentType == PaymentType.CARD;
    }

    private boolean needsBalanceCheck(PaymentType paymentType) {
        return paymentType == PaymentType.TRANSFER || paymentType == PaymentType.PIX;
    }

    private boolean processBalanceTransfer(Payment payment) {
        try {
            // Debitar da conta origem (se existir)
            if (payment.getFromAccount() != null && needsBalanceCheck(payment.getPaymentType())) {
                boolean debitSuccess = accountService.subtractBalance(
                        payment.getFromAccount().getId(),
                        payment.getAmount()
                );

                if (!debitSuccess) {
                    log.warn("Falha ao debitar conta origem: {}", payment.getFromAccount().getId());
                    return false;
                }
            }

            // Creditar na conta destino
            accountService.addBalance(payment.getToAccount().getId(), payment.getAmount());

            return true;

        } catch (Exception e) {
            log.error("Erro na transferência de saldo para pagamento {}", payment.getId(), e);
            return false;
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                "-" + System.currentTimeMillis();
    }

    private PaymentResponseDTO convertToResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .fromAccountId(payment.getFromAccount() != null ? payment.getFromAccount().getId() : null)
                .toAccountId(payment.getToAccount().getId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .externalId(payment.getExternalId())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}