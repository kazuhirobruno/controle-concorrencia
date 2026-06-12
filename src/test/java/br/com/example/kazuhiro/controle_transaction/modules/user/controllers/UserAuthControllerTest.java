package br.com.example.kazuhiro.controle_transaction.modules.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.AuthUserUseCase;
import br.com.example.kazuhiro.controle_transaction.providers.AuthJWTProvider;

@WebMvcTest(UserAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUserUseCase authUserUseCase;

    @MockitoBean
    private AuthJWTProvider authJWTProvider;

    private AuthUserRequestDTO validAuthRequest;
    private AuthUserResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        validAuthRequest = AuthUserRequestDTO.builder()
                .username("testuser")
                .password("secret")
                .build();

        mockResponse = AuthUserResponseDTO.builder()
                .access_token("jwt-token-de-teste-1234")
                .build();
    }

    @Test
    @DisplayName("Deve autenticar um cliente com sucesso e retornar o objeto de resposta com status 200 OK.")
    void shouldAuthenticateClientWithSuccess() throws Exception {
        Mockito.when(this.authUserUseCase.execute(Mockito.any(AuthUserRequestDTO.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/clientes/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(mockResponse)));
    }

    @Test
    @DisplayName("Deve retornar status 401 Unauthorized quando as credenciais forem inválidas ou o UseCase lançar exceção.")
    void shouldReturnUnauthorizedWhenAuthenticationFails() throws Exception {
        String mensagemErroEsperada = "Usuário ou senha incorretos";
        Mockito.when(this.authUserUseCase.execute(Mockito.any(AuthUserRequestDTO.class)))
                .thenThrow(new RuntimeException(mensagemErroEsperada));

        mockMvc.perform(MockMvcRequestBuilders.post("/clientes/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(mensagemErroEsperada));
    }
}