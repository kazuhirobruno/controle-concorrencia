package br.com.example.kazuhiro.controle_transaction.modules.user.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.CreateUserUseCase;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc; // Envia as requisições HTTP simuladas

  @Autowired
  private ObjectMapper objectMapper; // Transforma objetos Java em JSON

  @MockitoBean
  private CreateUserUseCase createUserUseCase; // Cria um mock do seu UseCase

  private UserEntity validUser;

  @BeforeEach
  void setUp() {
    // 2. Inicializa um objeto de teste limpo antes de cada execução
    validUser = UserEntity.builder()
        .username("test")
        .password("test")
        .limite(1000)
        .saldo(0).build();
  }

  @Test
  @DisplayName("Deve criar um usuário com sucesso e retornar status 200 OK.")
  void shouldCreateUserWithSuccess() throws Exception {
    // Mock do retorno do UseCase
    when(createUserUseCase.execute(any(UserEntity.class))).thenReturn(validUser);

    // Executa a requisição POST simulada
    mockMvc.perform(MockMvcRequestBuilders.post("/clientes/") // Ajuste para a rota real do seu controller
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUser)))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  @DisplayName("Deve retornar status 400 Bad Request existir usuário com username salvo.")
  @WithMockUser
  void shouldReturnBadRequestWhenUseCaseThrowsException() throws Exception {
    String errorMessage = "Usuário existente.";

    when(createUserUseCase.execute(any(UserEntity.class)))
        .thenThrow(new RuntimeException(errorMessage));

    mockMvc.perform(MockMvcRequestBuilders.post("/clientes/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUser)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.content().string(errorMessage));
  }
}
