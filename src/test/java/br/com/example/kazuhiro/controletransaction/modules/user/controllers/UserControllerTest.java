package br.com.example.kazuhiro.controletransaction.modules.user.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.example.kazuhiro.controletransaction.exceptions.PasswordNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.CreateUserDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.ChangeUserPasswordUseCase;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.CreateUserUseCase;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.DeleteUserUseCase;
import br.com.example.kazuhiro.controletransaction.providers.AuthJWTProvider;

@WebMvcTest({ UserController.class })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CreateUserUseCase createUserUseCase;

	@MockitoBean
	private ChangeUserPasswordUseCase changeUserPasswordUseCase;

	@MockitoBean
	private DeleteUserUseCase deleteUserUseCase;

	@MockitoBean
	private AuthJWTProvider authJWTProvider;

	private UserEntity validUser;
	private CreateUserDTO validCreateDto;
	private ChangeUserPasswordDTO validPasswordDto;
	private UUID mockClientId;

	@BeforeEach
	void setUp() {
		mockClientId = UUID.randomUUID();

		validUser = UserEntity.builder()
				.id(mockClientId)
				.username("test")
				.password("test")
				.limite(1000)
				.saldo(0)
				.build();

		validCreateDto = CreateUserDTO.builder()
				.username("test")
				.password("test")
				.confirmPassword("test")
				.limite(1000)
				.saldo(0)
				.build();

		validPasswordDto = ChangeUserPasswordDTO.builder()
				.newPassword("novaSenha123")
				.confirmNewPassword("novaSenha123")
				.build();
	}

	@Test
	@DisplayName("Deve criar um usuário com sucesso e retornar status 201 Created.")
	void shouldCreateUserWithSuccess() throws Exception {
		when(createUserUseCase.execute(any(CreateUserDTO.class))).thenReturn(validUser);

		mockMvc.perform(MockMvcRequestBuilders.post("/clientes/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCreateDto)))
				.andExpect(MockMvcResultMatchers.status().isCreated());
	}

	@Test
	@DisplayName("Deve retornar status 400 Bad Request ao lançar PasswordNotMatchesException ou UserFoundException")
	void shouldReturnBadRequestWhenUserValidationFails() throws Exception {
		String errorMessage = "Senha e confirmação são diferentes.";
		when(createUserUseCase.execute(any(CreateUserDTO.class)))
				.thenThrow(new PasswordNotMatchesException());

		mockMvc.perform(MockMvcRequestBuilders.post("/clientes/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCreateDto)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 400 Bad Request com mensagem genérica em caso de exceção desconhecida no cadastro")
	void shouldReturnBadRequestWithGenericMessageWhenGenericExceptionOccursOnCreate() throws Exception {
		when(createUserUseCase.execute(any(CreateUserDTO.class)))
				.thenThrow(new RuntimeException("Erro grave de conectividade"));

		mockMvc.perform(MockMvcRequestBuilders.post("/clientes/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCreateDto)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string("Erro ao processar o cadastro."));
	}

	@Test
	@DisplayName("Deve retornar status 204 No Content quando a senha for alterada com sucesso.")
	void shouldReturn204WhenPasswordChangedSuccessfully() throws Exception {
		doNothing().when(changeUserPasswordUseCase).execute(any(ChangeUserPasswordDTO.class), any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.patch("/clientes/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validPasswordDto))
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar status 400 Bad Request na troca de senha se houver IllegalArgumentException")
	void shouldReturn400WhenChangePasswordThrowsIllegalArgumentException() throws Exception {
		String errorMessage = "Dados inválidos.";
		doThrow(new IllegalArgumentException(errorMessage))
				.when(changeUserPasswordUseCase)
				.execute(any(ChangeUserPasswordDTO.class), any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.patch("/clientes/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validPasswordDto))
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found se a troca de senha lançar UsernameNotFoundException")
	void shouldReturn404WhenChangePasswordThrowsUsernameNotFoundException() throws Exception {
		String errorMessage = "Recurso indisponível ou inexistente.";
		doThrow(new UsernameNotFoundException(errorMessage))
				.when(changeUserPasswordUseCase)
				.execute(any(ChangeUserPasswordDTO.class), any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.patch("/clientes/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validPasswordDto))
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 500 Internal Server Error se a troca de senha lançar uma exceção genérica")
	void shouldReturn500WhenChangePasswordThrowsGenericException() throws Exception {
		doThrow(new RuntimeException())
				.when(changeUserPasswordUseCase)
				.execute(any(ChangeUserPasswordDTO.class), any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.patch("/clientes/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validPasswordDto))
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(MockMvcResultMatchers.content().string("Erro interno ao processar a requisição."));
	}

	@Test
	@DisplayName("Deve retornar status 204 No Content quando o usuário for deletado com sucesso.")
	void shouldReturn204WhenUserDeletedSuccessfully() throws Exception {
		doNothing().when(deleteUserUseCase).execute(any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/")
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar status 404 Not Found se a deleção lançar UsernameNotFoundException")
	void shouldReturn404WhenDeleteUserThrowsUsernameNotFoundException() throws Exception {
		String errorMessage = "Recurso indisponível ou inexistente.";
		doThrow(new UsernameNotFoundException(errorMessage))
				.when(deleteUserUseCase)
				.execute(any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/")
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.content().string(errorMessage));
	}

	@Test
	@DisplayName("Deve retornar status 401 Unauthorized se a deleção lançar AuthenticationException")
	void shouldReturn401WhenDeleteUserThrowsAuthenticationException() throws Exception {
		doThrow(new AuthenticationException("Token expirado.") {
		})
				.when(deleteUserUseCase)
				.execute(any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/")
				.requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(MockMvcResultMatchers.content().string("Token JWT ausente, expirado ou inválido."));
	}

	@Test
	@DisplayName("Deve retornar status 500 Internal Server Error se a deleção lançar uma exceção genérica")
	void shouldReturn500WhenDeleteUserThrowsGenericException() throws Exception {
		doThrow(new RuntimeException())
				.when(deleteUserUseCase)
				.execute(any(UUID.class));

		mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/").requestAttr("cliente_id", mockClientId))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(MockMvcResultMatchers.content().string("Erro interno ao processar a requisição."));
	}
}