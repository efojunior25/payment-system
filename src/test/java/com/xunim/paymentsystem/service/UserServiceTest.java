package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.dto.request.CreateUserRequestDTO;
import com.xunim.paymentsystem.dto.response.UserResponseDTO;
import com.xunim.paymentsystem.entity.User;
import com.xunim.paymentsystem.exception.BusinessException;
import com.xunim.paymentsystem.exception.ResourceNotFoundException;
import com.xunim.paymentsystem.repository.mysql.UserRepository;
import com.xunim.paymentsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequestDTO validRequest;
    private User validUser;

    @BeforeEach
    void setUp() {
        validRequest = CreateUserRequestDTO.builder()
                .email("test@email.com")
                .document("12345678901")
                .fullName("Test User")
                .phone("11999999999")
                .build();

        validUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .document("12345678901")
                .fullName("Test User")
                .phone("11999999999")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByDocument(validRequest.getDocument())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // When
        UserResponseDTO response = userService.createUser(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@email.com");
        assertThat(response.getIsActive()).isTrue();

        verify(userRepository).save(any(User.class));
        verify(auditService).logAction(eq("USER"), eq("1"), eq("CREATE"), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando documento já existe")
    void shouldThrowExceptionWhenDocumentExists() {
        // Given
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByDocument(validRequest.getDocument())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Documento já cadastrado");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));

        // When
        UserResponseDTO response = userService.getUserById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve desativar usuário com sucesso")
    void shouldDeactivateUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // When
        userService.deactivateUser(1L);

        // Then
        verify(userRepository).save(argThat(user -> !user.getIsActive()));
        verify(auditService).logAction(eq("USER"), eq("1"), eq("UPDATE"), anyString());
    }
}