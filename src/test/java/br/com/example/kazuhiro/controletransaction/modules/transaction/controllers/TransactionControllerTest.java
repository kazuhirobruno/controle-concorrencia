package br.com.example.kazuhiro.controletransaction.modules.transaction.controllers;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.example.kazuhiro.controletransaction.exceptions.GlobalExceptionHandler;
import br.com.example.kazuhiro.controletransaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.usecases.CreateTransactionUseCase;
import br.com.example.kazuhiro.controletransaction.security.SecurityClienteFilter;

@WebMvcTest({ TransactionController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

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
				.valor(1000L)
				.descricao("Pix")
				.build();
	}

	@Test
	@DisplayName("Deve criar transação e retornar 200 OK")
	void shouldCreateTransactionAndReturnOk() throws Exception {
		CreateTransactionResponseDTO transactionResponse = CreateTransactionResponseDTO.builder()
				.limite(0L)
				.saldo(0L)
				.build();

		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenReturn(transactionResponse);

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(transactionResponse)));
	}

	@Test
	@DisplayName("Deve retornar status 401 Unauthorized quando o atributo cliente_id for nulo")
	void shouldReturnUnauthorizedWhenClientAttributeIsNull() throws Exception {
		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Token JWT ausente, expirado ou inválido."));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found ao lançar UserIdNotFoundException")
	void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
		String errorMessage = "Recurso indisponível ou inexistente.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new UserIdNotFoundException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isNotFound())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found ao lançar ResourceNotFoundException para usuário inativo")
	void shouldReturnNotFoundWhenUserIsInactive() throws Exception {
		String errorMessage = "Recurso não encontrado.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new ResourceNotFoundException(errorMessage));

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isNotFound())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 422 Unprocessable Entity quando o tipo de transação for inválido")
	void shouldReturnUnprocessableEntityWhenTransactionTypeIsIllegal() throws Exception {
		String expectedMessage = "Tipo de transação desconhecido: x";

		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new IllegalTransactionTypeException("x"));

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().string(expectedMessage));
	}

	@Test
	@DisplayName("Deve retornar 422 Unprocessable Entity quando o débito excede o limite")
	void shouldReturnUnprocessableEntityWhenLimitReached() throws Exception {
		String errorMessage = "Operação ilegal. Valor negativado supera o limite do usuário.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new LimitReachedException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 401 Unauthorized quando os ids do usuário divergem")
	void shouldReturnUnauthorizedWhenUserIdsDoNotMatch() throws Exception {
		String errorMessage = "UserId de URL e Token não são iguais.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new UserIdNotMatchesException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 401 Unauthorized ao lançar AuthenticationException")
	void shouldReturnUnauthorizedWhenAuthenticationExceptionOccurs() throws Exception {
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new AuthenticationException("Token expirado.") {
				});

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Token expirado."));
	}

	@Test
	@DisplayName("Deve retornar 500 Internal Server Error quando uma exceção genérica acontecer")
	void shouldReturnInternalServerErrorWhenGenericExceptionOccurs() throws Exception {
		when(createTransactionUseCase.execute(any(String.class), any(String.class),
				any(CreateTransactionRequestDTO.class)))
				.thenThrow(new RuntimeException("Erro genérico de banco"));

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("Erro interno ao processar a requisição."));
	}
}
