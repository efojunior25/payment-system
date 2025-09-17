package com.xunim.paymentsystem.service.impl;

import com.xunim.paymentsystem.dto.request.CreateAccountRequestDTO;
import com.xunim.paymentsystem.dto.response.AccountResponseDTO;
import com.xunim.paymentsystem.entity.Account;
import com.xunim.paymentsystem.entity.User;
import com.xunim.paymentsystem.enums.AccountStatus;
import com.xunim.paymentsystem.exception.BusinessException;
import com.xunim.paymentsystem.exception.ResourceNotFoundException;
import com.xunim.paymentsystem.repository.mysql.AccountRepository;
import com.xunim.paymentsystem.repository.mysql.UserRepository;
import com.xunim.paymentsystem.service.AccountService;
import com.xunim.paymentsystem.service.AuditService;
import com.xunim.paymentsystem.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
        log.info("Criando nova conta para usuário ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.getUserId()));

        if (!user.getIsActive()) {
            throw new BusinessException("Não é possível criar conta para usuário inativo");
        }

        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generateAccountNumber();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        auditService.logAction("ACCOUNT", savedAccount.getId().toString(), "CREATE",
                "Conta criada: " + savedAccount.getAccountNumber());

        log.info("Conta criada com sucesso: ID {}", savedAccount.getId());
        return convertToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(Long id) {
        log.debug("Buscando conta por ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));

        return convertToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        log.debug("Buscando conta por número: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + accountNumber));

        return convertToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsByUserId(Long userId) {
        log.debug("Buscando contas do usuário ID: {}", userId);

        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsByStatus(AccountStatus status) {
        log.debug("Buscando contas por status: {}", status);

        return accountRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void blockAccount(Long id) {
        log.info("Bloqueando conta ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Não é possível bloquear conta encerrada");
        }

        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);

        auditService.logAction("ACCOUNT", account.getId().toString(), "UPDATE",
                "Conta bloqueada: " + account.getAccountNumber());

        log.info("Conta bloqueada com sucesso: ID {}", id);
    }

    @Override
    public void unblockAccount(Long id) {
        log.info("Desbloqueando conta ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));

        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new BusinessException("Apenas contas bloqueadas podem ser desbloqueadas");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        auditService.logAction("ACCOUNT", account.getId().toString(), "UPDATE",
                "Conta desbloqueada: " + account.getAccountNumber());

        log.info("Conta desbloqueada com sucesso: ID {}", id);
    }

    @Override
    public void closeAccount(Long id) {
        log.info("Encerrando conta ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Não é possível encerrar conta com saldo");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        auditService.logAction("ACCOUNT", account.getId().toString(), "UPDATE",
                "Conta encerrada: " + account.getAccountNumber());

        log.info("Conta encerrada com sucesso: ID {}", id);
    }

    @Override
    public void addBalance(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor deve ser maior que zero");
        }

        accountRepository.addToBalance(accountId, amount);
        log.debug("Saldo adicionado à conta {}: {}", accountId, amount);
    }

    @Override
    public boolean subtractBalance(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor deve ser maior que zero");
        }

        int updated = accountRepository.subtractFromBalance(accountId, amount);
        boolean success = updated > 0;

        if (success) {
            log.debug("Saldo subtraído da conta {}: {}", accountId, amount);
        } else {
            log.warn("Falha ao subtrair saldo da conta {}: saldo insuficiente", accountId);
        }

        return success;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalActiveBalance() {
        BigDecimal total = accountRepository.getTotalActiveBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    private AccountResponseDTO convertToResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .userId(account.getUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}