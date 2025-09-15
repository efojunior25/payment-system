package com.xunim.paymentsystem.controller;

import com.xunim.paymentsystem.dto.request.CreateUserRequestDTO;
import com.xunim.paymentsystem.dto.response.UserResponseDTO;
import com.xunim.paymentsystem.service.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Operações relacionadas ao gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar novo usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado")
    })
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody CreateUserRequestDTO request) {

        log.info("Criando usuário: {}", request.getEmail());
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        log.debug("Buscando usuário por ID: {}", id);
        UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuário por email", description = "Retorna um usuário específico pelo email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(description = "Email do usuário") @PathVariable String email) {

        log.debug("Buscando usuário por email: {}", email);
        UserResponseDTO response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/document/{document}")
    @Operation(summary = "Buscar usuário por documento", description = "Retorna um usuário específico pelo documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponseDTO> getUserByDocument(
            @Parameter(description = "Documento do usuário (CPF/CNPJ)") @PathVariable String document) {

        log.debug("Buscando usuário por documento: {}", document);
        UserResponseDTO response = userService.getUserByDocument(document);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna uma lista paginada de todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Listando usuários - Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponseDTO> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar usuários ativos", description = "Retorna uma lista de todos os usuários ativos")
    @ApiResponse(responseCode = "200", description = "Lista de usuários ativos retornada com sucesso")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {

        log.debug("Listando usuários ativos");
        List<UserResponseDTO> response = userService.getAllActiveUsers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado")
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody CreateUserRequestDTO request) {

        log.info("Atualizando usuário ID: {}", id);
        UserResponseDTO response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        log.info("Desativando usuário ID: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar usuário", description = "Ativa um usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário ativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        log.info("Ativando usuário ID: {}", id);
        userService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "Verificar se email existe", description = "Verifica se um email já está cadastrado")
    @ApiResponse(responseCode = "200", description = "Status de existência do email retornado")
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email a ser verificado") @PathVariable String email) {

        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/document/{document}")
    @Operation(summary = "Verificar se documento existe", description = "Verifica se um documento já está cadastrado")
    @ApiResponse(responseCode = "200", description = "Status de existência do documento retornado")
    public ResponseEntity<Boolean> existsByDocument(
            @Parameter(description = "Documento a ser verificado") @PathVariable String document) {

        boolean exists = userService.existsByDocument(document);
        return ResponseEntity.ok(exists);
    }
}