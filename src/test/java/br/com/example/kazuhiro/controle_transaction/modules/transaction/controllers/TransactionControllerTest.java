package br.com.example.kazuhiro.controle_transaction.modules.transaction.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.example.kazuhiro.controle_transaction.exceptions.GlobalExceptionHandler;
import br.com.example.kazuhiro.controle_transaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controle_transaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controle_transaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.useCases.CreateTransactionUseCase;
import br.com.example.kazuhiro.controle_transaction.security.SecurityClienteFilter;

@WebMvcTest({ TransactionController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CreateTransactionUseCase createTransactionUseCase;

  @MockitoBean
  private SecurityClienteFilter securityClienteFilter;

  private CreateTransactionRequestDTO validRequest;
  private String validClientId;
  private String validTokenId;

  @BeforeEach
  void setUp() {
    validClientId = "00000000-0000-0000-0000-000000000001";
    validTokenId = validClientId;
    validRequest = CreateTransactionRequestDTO.builder()
        .tipo("c")
        .valor(1000)
        .descricao("Pix")
        .build();
  }

  @Test
  @DisplayName("Deve criar transação e retornar 200 OK")
  void shouldCreateTransactionAndReturnOk() throws Exception {
    CreateTransactionResponseDTO transactionResponse = CreateTransactionResponseDTO.builder()
        .limite(0)
        .saldo(0)
        .build();

    when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
        .thenReturn(transactionResponse);

    mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
        .requestAttr("cliente_id", validTokenId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(transactionResponse)));
  }

  @Test
  @DisplayName("Deve retornar 401 Unauthorized quando os ids do usuário divergem")
  void shouldReturnUnauthorizedWhenUserIdsDoNotMatch() throws Exception {
    when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
        .thenThrow(new UserIdNotMatchesException());

    mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
        .requestAttr("cliente_id", "00000000-0000-0000-0000-000000000002")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Deve retornar 422 Unprocessable Entity quando o tipo de transação for inválido")
  void shouldReturnUnauthorizedWhenTransactionTypeIsIllegal() throws Exception {
    when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
        .thenThrow(new IllegalTransactionTypeException("x"));

    validRequest.setTipo("x");

    mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
        .requestAttr("cliente_id", validTokenId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string("Tipo de transação desconhecido: x"));
  }

  @Test
  @DisplayName("Deve retornar 422 Unprocessable Entity quando o débito excede o limite")
  void shouldReturnUnauthorizedWhenLimitReached() throws Exception {
    when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
        .thenThrow(new LimitReachedException());

    validRequest.setTipo("d");
    validRequest.setValor(1500);

    mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
        .requestAttr("cliente_id", validTokenId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string("Operação ilegal. Valor negativado supera o limite do usuário."));
  }

  @Test
  @DisplayName("Deve retornar 422 quando a descrição ultrapassar 10 caracteres")
  void shouldReturnUnprocessableEntityWhenDescriptionIsTooLong() throws Exception {
    validRequest.setDescricao("Texto Muito Longo");

    mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
        .requestAttr("cliente_id", validTokenId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string("A descrição deve conter entre 1 e 10 caracteres."));
  }
}
