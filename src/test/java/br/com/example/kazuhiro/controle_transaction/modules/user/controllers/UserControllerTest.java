package br.com.example.kazuhiro.controle_transaction.modules.user.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.CreateUserUseCase;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.DeleteUserUseCase;
import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.ChangeUserPasswordUseCase;
import br.com.example.kazuhiro.controle_transaction.providers.AuthJWTProvider;

import java.util.UUID;

@WebMvcTest({ UserController.class })
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

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
        private ChangeUserPasswordDTO validPasswordDto;
        private UUID mockClientId;

        @BeforeEach
        void setUp() {
                mockClientId = UUID.randomUUID();

                validUser = UserEntity.builder()
                                .username("test")
                                .password("test")
                                .limite(1000)
                                .saldo(0)
                                .build();

                validPasswordDto = ChangeUserPasswordDTO.builder()
                                .newPassword("novaSenha123")
                                .confirmNewPassword("novaSenha123")
                                .build();
        }

        @Test
        @DisplayName("Deve criar um usuário com sucesso e retornar status 200 OK.")
        void shouldCreateUserWithSuccess() throws Exception {
                when(createUserUseCase.execute(any(UserEntity.class))).thenReturn(validUser);

                mockMvc.perform(MockMvcRequestBuilders.post("/clientes/")
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
        @DisplayName("Deve retornar status 400 Bad Request quando as senhas enviadas forem diferentes.")
        void shouldReturn400WhenPasswordsDoNotMatch() throws Exception {
                String errorMessage = "A nova senha e a confirmação de senha não coincidem.";

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
        @DisplayName("Deve retornar status 404 Not Found quando o cliente não for encontrado no sistema.")
        void shouldReturn404WhenUserNotFound() throws Exception {
                String errorMessage = "Usuário não encontrado.";

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
        @DisplayName("Deve retornar status 204 No Content quando o usuário for deletado com sucesso.")
        void shouldReturn204WhenUserDeletedSuccessfully() throws Exception {
                doNothing().when(deleteUserUseCase).execute(any(UUID.class));

                mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/delete")
                                .requestAttr("cliente_id", mockClientId))
                                .andExpect(MockMvcResultMatchers.status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 Not Found quando o usuário a ser deletado não existir.")
        void shouldReturn404WhenUserToDeleteNotFound() throws Exception {
                doThrow(new UsernameNotFoundException("Usuário não encontrado."))
                                .when(deleteUserUseCase).execute(any(UUID.class));

                mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/delete")
                                .requestAttr("cliente_id", mockClientId))
                                .andExpect(MockMvcResultMatchers.status().isNotFound())
                                .andExpect(MockMvcResultMatchers.content().string("Usuário não encontrado."));
        }

        @Test
        @DisplayName("Deve retornar status 401 Unauthorized quando ocorrer falha de autenticação ao deletar.")
        void shouldReturn401WhenAuthenticationFailsOnDelete() throws Exception {
                doThrow(new AuthenticationException("Token JWT ausente, expirado ou inválido.") {
                })
                                .when(deleteUserUseCase).execute(any(UUID.class));

                mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/delete")
                                .requestAttr("cliente_id", mockClientId))
                                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                                .andExpect(MockMvcResultMatchers.content()
                                                .string("Token JWT ausente, expirado ou inválido."));
        }
}