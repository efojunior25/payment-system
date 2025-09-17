package com.xunim.paymentsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunim.paymentsystem.dto.request.CreateUserRequestDTO;
import com.xunim.paymentsystem.repository.mysql.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("xunimpay_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7")
            .withExposedPorts(27017)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CreateUserRequestDTO validUserRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validUserRequest = CreateUserRequestDTO.builder()
                .email("integration@test.com")
                .document("12345678901")
                .fullName("Integration Test User")
                .phone("11999999999")
                .build();
    }

    @Test
    @DisplayName("Deve criar usuário via API com sucesso")
    void shouldCreateUserViaApiSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("integration@test.com")))
                .andExpect(jsonPath("$.fullName", is("Integration Test User")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para dados inválidos")
    void shouldReturn400ForInvalidData() throws Exception {
        CreateUserRequestDTO invalidRequest = CreateUserRequestDTO.builder()
                .email("invalid-email")
                .document("123") // Documento muito curto
                .fullName("") // Nome vazio
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para email duplicado")
    void shouldReturn400ForDuplicateEmail() throws Exception {
        // Primeiro usuário
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Segundo usuário com mesmo email
        CreateUserRequestDTO duplicateEmailRequest = CreateUserRequestDTO.builder()
                .email("integration@test.com") // Email duplicado
                .document("98765432100")
                .fullName("Another User")
                .phone("11888888888")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email já cadastrado")));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    @WithMockUser
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Criar usuário primeiro
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrair ID do usuário criado
        var userResponse = objectMapper.readTree(response);
        Long userId = userResponse.get("id").asLong();

        // Buscar usuário por ID
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.email", is("integration@test.com")));
    }

    @Test
    @DisplayName("Deve retornar 404 para usuário não encontrado")
    @WithMockUser
    void shouldReturn404ForUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    @WithMockUser
    void shouldGetUserByEmailSuccessfully() throws Exception {
        // Criar usuário primeiro
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Buscar usuário por email
        mockMvc.perform(get("/api/v1/users/email/integration@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("integration@test.com")))
                .andExpect(jsonPath("$.fullName", is("Integration Test User")));
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() throws Exception {
        // Verificar email que não existe
        mockMvc.perform(get("/api/v1/users/exists/email/nonexistent@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Criar usuário
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Verificar email que existe
        mockMvc.perform(get("/api/v1/users/exists/email/integration@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Deve listar usuários ativos")
    @WithMockUser
    void shouldListActiveUsers() throws Exception {
        // Criar usuário
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Listar usuários ativos
        mockMvc.perform(get("/api/v1/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("integration@test.com")));
    }

    @Test
    @DisplayName("Deve desativar usuário")
    @WithMockUser
    void shouldDeactivateUser() throws Exception {
        // Criar usuário
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var userResponse = objectMapper.readTree(response);
        Long userId = userResponse.get("id").asLong();

        // Desativar usuário
        mockMvc.perform(patch("/api/v1/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        // Verificar se usuário foi desativado
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));
    }
}