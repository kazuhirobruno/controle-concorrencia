package br.com.example.kazuhiro.controletransaction.modules.transaction.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.ExtratoResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.SaldoDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.TransacaoDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.usecases.CreateTransactionUseCase;
import br.com.example.kazuhiro.controletransaction.modules.transaction.usecases.GetExtratoUseCase;
import br.com.example.kazuhiro.controletransaction.security.SecurityClienteFilter;

import java.time.Instant;
import java.util.List;

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
	private GetExtratoUseCase getExtratoUseCase;

	@MockitoBean
	private SecurityClienteFilter securityClienteFilter;

	private CreateTransactionRequestDTO validRequest;
	private String validClientId;
	private String validTokenId;
	private ExtratoResponseDTO extratoResponse;

	private static final Instant DATA_FIXA_2060 = Instant.parse("2060-01-17T00:00:00Z");

	@BeforeEach
	void setUp() {
		validClientId = "00000000-0000-0000-0000-000000000001";
		validTokenId = validClientId;

		validRequest = CreateTransactionRequestDTO.builder()
				.tipo("c")
				.valor(1000L)
				.descricao("Pix")
				.build();

		TransacaoDTO transacao = TransacaoDTO.builder()
				.valor(100L)
				.tipo('c')
				.descricao("Deposito")
				.realizadaEm(DATA_FIXA_2060)
				.build();

		SaldoDTO saldo = SaldoDTO.builder()
				.total(-1000L)
				.limite(100000L)
				.dataExtrato(DATA_FIXA_2060)
				.build();

		extratoResponse = ExtratoResponseDTO.builder()
				.saldo(saldo)
				.ultimasTransacoes(List.of(transacao))
				.build();
	}

	@Test
	@DisplayName("Deve criar transação e retornar 200 OK")
	void shouldCreateTransactionAndReturnOk() throws Exception {
		CreateTransactionResponseDTO transactionResponse = CreateTransactionResponseDTO.builder()
				.limite(0L)
				.saldo(0L)
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
	@DisplayName("Deve retornar status 401 Unauthorized quando o atributo cliente_id for nulo no POST")
	void shouldReturnUnauthorizedWhenClientAttributeIsNull() throws Exception {
		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Token JWT ausente, expirado ou inválido."));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found ao lançar UserIdNotFoundException no POST")
	void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
		String errorMessage = "Recurso indisponível ou inexistente.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new UserIdNotFoundException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isNotFound())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found ao lançar ResourceNotFoundException no POST")
	void shouldReturnNotFoundWhenUserIsInactive() throws Exception {
		String errorMessage = "Recurso não encontrado.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new ResourceNotFoundException(errorMessage));

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isNotFound())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 422 Unprocessable Entity quando o tipo de transação for inválido no POST")
	void shouldReturnUnprocessableEntityWhenTransactionTypeIsIllegal() throws Exception {
		String expectedMessage = "Tipo de transação desconhecido: x";
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
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
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new LimitReachedException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 401 Unauthorized quando os ids do usuário divergem no POST")
	void shouldReturnUnauthorizedWhenUserIdsDoNotMatch() throws Exception {
		String errorMessage = "UserId de URL e Token não são iguais.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new UserIdNotMatchesException());

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 401 Unauthorized ao lançar AuthenticationException no POST")
	void shouldReturnUnauthorizedWhenAuthenticationExceptionOccurs() throws Exception {
		String errorMessage = "Token expirado.";
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new AuthenticationException(errorMessage) {
				});

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar 500 Internal Server Error ao estourar erro genérico no POST")
	void shouldReturnInternalServerErrorWhenGenericExceptionOccursOnPost() throws Exception {
		when(createTransactionUseCase.execute(any(String.class), any(String.class), any(CreateTransactionRequestDTO.class)))
				.thenThrow(new RuntimeException("Database offline"));

		mockMvc.perform(post("/clientes/" + validClientId + "/transacoes")
				.requestAttr("cliente_id", validTokenId)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("Erro interno ao processar a requisição."));
	}

	@Test
	@DisplayName("Deve retornar extrato com sucesso 200 OK")
	void shouldReturnExtratoSuccessfully() throws Exception {
		when(getExtratoUseCase.execute(validClientId, validTokenId)).thenReturn(extratoResponse);
		mockMvc.perform(get("/clientes/" + validClientId + "/extrato").requestAttr("cliente_id", validTokenId))
				.andExpect(status().isOk()).andExpect(content().json(objectMapper.writeValueAsString(extratoResponse)));
	}

	@Test
	@DisplayName("Deve retornar status 401 Unauthorized quando o atributo cliente_id for nulo no GET")
	void shouldReturnUnauthorizedWhenClientAttributeIsNullOnGet() throws Exception {
		mockMvc.perform(get("/clientes/" + validClientId + "/extrato")).andExpect(status().isUnauthorized())
				.andExpect(content().string("Token JWT ausente, expirado ou inválido."));
	}

	@Test
	@DisplayName("Deve retornar status 422 Unprocessable Entity quando o id da URL for um UUID inválido no GET")
	void shouldReturnUnprocessableEntityWhenUuidIsMalformed() throws Exception {
		mockMvc.perform(get("/clientes/id-invalido-123/extrato").requestAttr("cliente_id", validTokenId))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("O ID fornecido não é um formato válido."));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found ao lançar UserIdNotFoundException no GET")
	void shouldReturnNotFoundWhenUserDoesNotExistOnGet() throws Exception {
		String errorMessage = "Recurso indisponível ou inexistente.";
		when(getExtratoUseCase.execute(validClientId, validTokenId)).thenThrow(new UserIdNotFoundException());
		mockMvc.perform(get("/clientes/" + validClientId + "/extrato").requestAttr("cliente_id", validTokenId))
				.andExpect(status().isNotFound()).andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 401 Unauthorized ao lançar UserIdNotMatchesException no GET")
	void shouldReturnUnauthorizedWhenUserIdsDoNotMatchOnGet() throws Exception {
		String errorMessage = "UserId de URL e Token não são iguais.";
		when(getExtratoUseCase.execute(validClientId, validTokenId)).thenThrow(new UserIdNotMatchesException());
		mockMvc.perform(get("/clientes/" + validClientId + "/extrato").requestAttr("cliente_id", validTokenId))
				.andExpect(status().isUnauthorized()).andExpect(content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 500 Internal Server Error ao estourar erro genérico no GET")
	void shouldReturnInternalServerErrorWhenGenericExceptionOccursOnGet() throws Exception {
		when(getExtratoUseCase.execute(validClientId, validTokenId)).thenThrow(new RuntimeException("Connection timeout"));
		mockMvc.perform(get("/clientes/" + validClientId + "/extrato").requestAttr("cliente_id", validTokenId))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("Erro interno ao processar a requisição."));
	}
}