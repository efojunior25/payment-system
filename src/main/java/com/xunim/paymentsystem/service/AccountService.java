package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.dto.request.CreateAccountRequestDTO;
import com.xunim.paymentsystem.dto.response.AccountResponseDTO;
import com.xunim.paymentsystem.enums.AccountStatus;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponseDTO createAccount(CreateAccountRequestDTO request);

    AccountResponseDTO getAccountById(Long id);

    AccountResponseDTO getAccountByNumber(String accountNumber);

    List<AccountResponseDTO> getAccountsByUserId(Long userId);

    List<AccountResponseDTO> getAccountsByStatus(AccountStatus status);

    void blockAccount(Long id);

    void unblockAccount(Long id);

    void closeAccount(Long id);

    void addBalance(Long accountId, BigDecimal amount);

    boolean subtractBalance(Long accountId, BigDecimal amount);

    BigDecimal getTotalActiveBalance();
}