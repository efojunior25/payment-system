package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.dto.request.CreateAccountRequestDTO;
import com.xunim.paymentsystem.dto.response.AccountResponseDTO;
import com.xunim.paymentsystem.entity.Account;
import com.xunim.paymentsystem.entity.User;
import com.xunim.paymentsystem.enums.AccountStatus;
import com.xunim.paymentsystem.exception.BusinessException;
import com.xunim.paymentsystem.exception.ResourceNotFoundException;
import com.xunim.paymentsystem.repository.mysql.AccountRepository;
import com.xunim.paymentsystem.repository.mysql.UserRepository;
import com.xunim.paymentsystem.service.impl.AccountServiceImpl;
import com.xunim.paymentsystem.util.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private AccountServiceImpl accountService;

    private CreateAccountRequestDTO validRequest;
    private User validUser;
    private Account validAccount;

    @BeforeEach
    void setUp() {
        validRequest = CreateAccountRequestDTO.builder()
                .userId(1L)
                .build();

        validUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .document("12345678901")
                .fullName("Test User")
                .isActive(true)
                .build();

        validAccount = Account.builder()
                .id(1L)
                .user(validUser)
                .accountNumber("001-123456-7")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void shouldCreateAccountSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(accountNumberGenerator.generateAccountNumber()).thenReturn("001-123456-7");
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        // When
        AccountResponseDTO response = accountService.createAccount(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAccountNumber()).isEqualTo("001-123456-7");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        verify(accountRepository).save(any(Account.class));
        verify(auditService).logAction(eq("ACCOUNT"), eq("1"), eq("CREATE"), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário está inativo")
    void shouldThrowExceptionWhenUserIsInactive() {
        // Given
        User inactiveUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .isActive(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Não é possível criar conta para usuário inativo");

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve bloquear conta com sucesso")
    void shouldBlockAccountSuccessfully() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        // When
        accountService.blockAccount(1L);

        // Then
        verify(accountRepository).save(argThat(account ->
                account.getStatus() == AccountStatus.BLOCKED));
        verify(auditService).logAction(eq("ACCOUNT"), eq("1"), eq("UPDATE"), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar bloquear conta encerrada")
    void shouldThrowExceptionWhenBlockingClosedAccount() {
        // Given
        Account closedAccount = Account.builder()
                .id(1L)
                .user(validUser)
                .status(AccountStatus.CLOSED)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(closedAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.blockAccount(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Não é possível bloquear conta encerrada");

        verify(accountRepository, never()).save(any(Account.class));
    }
}