package com.xunim.paymentsystem.service;

import com.xunim.paymentsystem.dto.request.CreateUserRequestDTO;
import com.xunim.paymentsystem.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO request);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO getUserByDocument(String document);

    List<UserResponseDTO> getAllActiveUsers();

    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    UserResponseDTO updateUser(Long id, CreateUserRequestDTO request);

    void deactivateUser(Long id);

    void activateUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);
}
