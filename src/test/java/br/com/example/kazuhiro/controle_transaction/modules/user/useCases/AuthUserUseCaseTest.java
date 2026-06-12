package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.security.sasl.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.Assertions;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthUserUseCase authUserUseCase;

  private AuthUserRequestDTO authRequest;
  private UserEntity existingUser;
  private final String mockSecretKey = "minha_chave_secreta_de_teste_12345";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authUserUseCase, "secretKey", mockSecretKey);

    authRequest = AuthUserRequestDTO.builder()
        .username("teste")
        .password("teste")
        .build();

    existingUser = UserEntity.builder()
        .id(UUID.randomUUID())
        .username("teste")
        .password("senha_criptografada_no_banco")
        .build();
  }

  @Test
  @DisplayName("Deve autenticar o usuário com sucesso e retornar o token JWT com as roles.")
  void shouldAuthenticateUserWithSuccess() throws AuthenticationException {
    when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.matches(authRequest.getPassword(), existingUser.getPassword())).thenReturn(true);

    AuthUserResponseDTO response = authUserUseCase.execute(authRequest);

    Assertions.assertThat(response).isNotNull();
    Assertions.assertThat(response.getAccess_token()).isNotBlank();
    Assertions.assertThat(response.getExpires_in()).isGreaterThan(0L);
    Assertions.assertThat(response.getRoles()).containsExactly("CLIENT");

    verify(userRepository).findByUsername(authRequest.getUsername());
    verify(passwordEncoder).matches(authRequest.getPassword(), existingUser.getPassword());
  }

  @Test
  @DisplayName("Deve lançar UsernameNotFoundException quando o username não for encontrado.")
  void shouldThrowExceptionWhenUsernameNotFound() {
    when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> authUserUseCase.execute(authRequest))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Username ou Password incorreto.");

    verify(passwordEncoder, never()).matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
  }

  @Test
  @DisplayName("Deve lançar AuthenticationException quando a senha informada estiver incorreta.")
  void shouldThrowExceptionWhenPasswordDoesNotMatch() {
    when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.matches(authRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

    Assertions.assertThatThrownBy(() -> authUserUseCase.execute(authRequest))
        .isInstanceOf(AuthenticationException.class);

    verify(userRepository).findByUsername(authRequest.getUsername());
    verify(passwordEncoder).matches(authRequest.getPassword(), existingUser.getPassword());
  }
}