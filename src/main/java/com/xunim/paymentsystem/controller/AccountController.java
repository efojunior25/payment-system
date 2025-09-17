package com.xunim.paymentsystem.controller;

import com.xunim.paymentsystem.dto.request.CreateAccountRequestDTO;
import com.xunim.paymentsystem.dto.response.AccountResponseDTO;
import com.xunim.paymentsystem.enums.AccountStatus;
import com.xunim.paymentsystem.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contas", description = "Operações relacionadas ao gerenciamento de contas")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Criar nova conta", description = "Cria uma nova conta para um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody CreateAccountRequestDTO request) {

        log.info("Criando conta para usuário ID: {}", request.getUserId());
        AccountResponseDTO response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID", description = "Retorna uma conta específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponseDTO> getAccountById(
            @Parameter(description = "ID da conta") @PathVariable Long id) {

        log.debug("Buscando conta por ID: {}", id);
        AccountResponseDTO response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Buscar conta por número", description = "Retorna uma conta específica pelo número")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(
            @Parameter(description = "Número da conta") @PathVariable String accountNumber) {

        log.debug("Buscando conta por número: {}", accountNumber);
        AccountResponseDTO response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar contas por usuário", description = "Retorna todas as contas de um usuário")
    @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(
            @Parameter(description = "ID do usuário") @PathVariable Long userId) {

        log.debug("Buscando contas do usuário ID: {}", userId);
        List<AccountResponseDTO> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar contas por status", description = "Retorna todas as contas com determinado status")
    @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByStatus(
            @Parameter(description = "Status da conta") @PathVariable AccountStatus status) {

        log.debug("Buscando contas por status: {}", status);
        List<AccountResponseDTO> response = accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Bloquear conta", description = "Bloqueia uma conta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta bloqueada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Operação inválida")
    })
    public ResponseEntity<Void> blockAccount(
            @Parameter(description = "ID da conta") @PathVariable Long id) {

        log.info("Bloqueando conta ID: {}", id);
        accountService.blockAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Desbloquear conta", description = "Desbloqueia uma conta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta desbloqueada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Operação inválida")
    })
    public ResponseEntity<Void> unblockAccount(
            @Parameter(description = "ID da conta") @PathVariable Long id) {

        log.info("Desbloqueando conta ID: {}", id);
        accountService.unblockAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Encerrar conta", description = "Encerra uma conta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta encerrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Operação inválida")
    })
    public ResponseEntity<Void> closeAccount(
            @Parameter(description = "ID da conta") @PathVariable Long id) {

        log.info("Encerrando conta ID: {}", id);
        accountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Saldo total ativo", description = "Retorna o saldo total de todas as contas ativas")
    @ApiResponse(responseCode = "200", description = "Saldo total retornado com sucesso")
    public ResponseEntity<BigDecimal> getTotalActiveBalance() {

        log.debug("Buscando saldo total ativo");
        BigDecimal total = accountService.getTotalActiveBalance();
        return ResponseEntity.ok(total);
    }
}