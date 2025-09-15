package com.xunim.paymentsystem.service.impl;

import com.xunim.paymentsystem.dto.request.CreateUserRequestDTO;
import com.xunim.paymentsystem.dto.response.AccountResponseDTO;
import com.xunim.paymentsystem.dto.response.UserResponseDTO;
import com.xunim.paymentsystem.entity.User;
import com.xunim.paymentsystem.exception.BusinessException;
import com.xunim.paymentsystem.exception.ResourceNotFoundException;
import com.xunim.paymentsystem.repository.mysql.UserRepository;
import com.xunim.paymentsystem.service.AuditService;
import com.xunim.paymentsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        log.info("Criando novo usuário com email: {}", request.getEmail());

        // Validações de negocio
        if (existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já Cadastrado: " + request.getEmail());
        }

        if (existsByDocument(request.getDocument())) {
            throw new BusinessException("Documento já cadastrado: " + request.getDocument());

        }

        // User Creation
        User user = User.builder()
                .email(request.getEmail())
                .document(request.getDocument())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Audit
        auditService.logAction("USER", savedUser.getId().toString(), "CREATE",
                "Usuário criado: " + savedUser.getEmail());

        log.info("Usuário criado com sucesso: ID {}", savedUser.getId());
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.debug("Buscando usuário por ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Buscando usuário por email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para email: " + email));

        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByDocument(String document) {
        log.debug("Buscando usuário por documento: {}", document);

        User user = userRepository.findByDocument(document)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para documento: " + document));

        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllActiveUsers() {
        log.debug("Buscando todos os usuários ativos");

        return userRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.debug("Buscando usuários paginados: {}", pageable);

        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Override
    public UserResponseDTO updateUser(Long id, CreateUserRequestDTO request) {
        log.info("Atualizando usuário ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        // Validar email único (se diferente do atual)
        if (!existingUser.getEmail().equals(request.getEmail()) && existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + request.getEmail());
        }

        // Validar documento único (se diferente do atual)
        if (!existingUser.getDocument().equals(request.getDocument()) && existsByDocument(request.getDocument())) {
            throw new BusinessException("Documento já cadastrado: " + request.getDocument());
        }

        // Atualizar dados
        existingUser.setEmail(request.getEmail());
        existingUser.setDocument(request.getDocument());
        existingUser.setFullName(request.getFullName());
        existingUser.setPhone(request.getPhone());

        User updatedUser = userRepository.save(existingUser);

        // Auditoria
        auditService.logAction("USER", updatedUser.getId().toString(), "UPDATE",
                "Usuário atualizado: " + updatedUser.getEmail());

        log.info("Usuário atualizado com sucesso: ID {}", updatedUser.getId());
        return convertToResponse(updatedUser);
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Desativando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        // Auditoria
        auditService.logAction("USER", user.getId().toString(), "UPDATE",
                "Usuário desativado: " + user.getEmail());

        log.info("Usuário desativado com sucesso: ID {}", id);
    }

    @Override
    public void activateUser(Long id) {
        log.info("Ativando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        // Auditoria
        auditService.logAction("USER", user.getId().toString(), "UPDATE",
                "Usuário ativado: " + user.getEmail());

        log.info("Usuário ativado com sucesso: ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDocument(String document) {
        return userRepository.existsByDocument(document);
    }

    private UserResponseDTO convertToResponse(User user) {
        List<AccountResponseDTO> accounts = null;
        if (user.getAccounts() != null) {
            accounts = user.getAccounts().stream()
                    .map(account -> AccountResponseDTO.builder()
                            .id(account.getId())
                            .userId(account.getUser().getId())
                            .accountNumber(account.getAccountNumber())
                            .balance(account.getBalance())
                            .status(account.getStatus())
                            .createdAt(account.getCreatedAt())
                            .updatedAt(account.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .document(user.getDocument())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .accounts(accounts)
                .build();
    }
}
