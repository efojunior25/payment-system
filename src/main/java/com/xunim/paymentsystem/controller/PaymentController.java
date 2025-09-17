package com.xunim.paymentsystem.controller;

import com.xunim.paymentsystem.dto.request.CreatePaymentRequestDTO;
import com.xunim.paymentsystem.dto.response.PaymentResponseDTO;
import com.xunim.paymentsystem.enums.PaymentStatus;
import com.xunim.paymentsystem.enums.PaymentType;
import com.xunim.paymentsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagamentos", description = "Operações relacionadas ao gerenciamento de pagamentos")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Criar novo pagamento", description = "Cria um novo pagamento no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request) {

        log.info("Criando pagamento: {} -> {}", request.getFromAccountId(), request.getToAccountId());
        PaymentResponseDTO response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID", description = "Retorna um pagamento específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "ID do pagamento") @PathVariable Long id) {

        log.debug("Buscando pagamento por ID: {}", id);
        PaymentResponseDTO response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Buscar pagamento por ID da transação", description = "Retorna um pagamento pelo ID da transação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentByTransactionId(
            @Parameter(description = "ID da transação") @PathVariable String transactionId) {

        log.debug("Buscando pagamento por transaction ID: {}", transactionId);
        PaymentResponseDTO response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Buscar pagamentos por conta", description = "Retorna pagamentos paginados de uma conta")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos retornada com sucesso")
    public ResponseEntity<Page<PaymentResponseDTO>> getPaymentsByAccount(
            @Parameter(description = "ID da conta") @PathVariable Long accountId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Buscando pagamentos da conta ID: {}", accountId);
        Page<PaymentResponseDTO> response = paymentService.getPaymentsByAccount(accountId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pagamentos por status", description = "Retorna todos os pagamentos com determinado status")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos retornada com sucesso")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(
            @Parameter(description = "Status do pagamento") @PathVariable PaymentStatus status) {

        log.debug("Buscando pagamentos por status: {}", status);
        List<PaymentResponseDTO> response = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Buscar pagamentos por período", description = "Retorna pagamentos em um período específico")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos retornada com sucesso")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByDateRange(
            @Parameter(description = "Data início") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Data fim") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        log.debug("Buscando pagamentos por período: {} - {}", start, end);
        List<PaymentResponseDTO> response = paymentService.getPaymentsByDateRange(start, end);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/process")
    @Operation(summary = "Processar pagamento", description = "Processa um pagamento pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento processado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "400", description = "Operação inválida")
    })
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Parameter(description = "ID do pagamento") @PathVariable Long id) {

        log.info("Processando pagamento ID: {}", id);
        PaymentResponseDTO response = paymentService.processPayment(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pagamento", description = "Cancela um pagamento pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "400", description = "Operação inválida")
    })
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @Parameter(description = "ID do pagamento") @PathVariable Long id) {

        log.info("Cancelando pagamento ID: {}", id);
        PaymentResponseDTO response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-by-type/{type}")
    @Operation(summary = "Total por tipo", description = "Retorna o total de pagamentos por tipo")
    @ApiResponse(responseCode = "200", description = "Total retornado com sucesso")
    public ResponseEntity<BigDecimal> getTotalAmountByType(
            @Parameter(description = "Tipo do pagamento") @PathVariable PaymentType type) {

        log.debug("Buscando total por tipo: {}", type);
        BigDecimal total = paymentService.getTotalAmountByType(type);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/count/{status}")
    @Operation(summary = "Contar por status desde data", description = "Conta pagamentos por status desde uma data")
    @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso")
    public ResponseEntity<Long> countPaymentsByStatusSince(
            @Parameter(description = "Status do pagamento") @PathVariable PaymentStatus status,
            @Parameter(description = "Data de referência") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        log.debug("Contando pagamentos por status {} desde {}", status, since);
        long count = paymentService.countPaymentsByStatusSince(status, since);
        return ResponseEntity.ok(count);
    }
}