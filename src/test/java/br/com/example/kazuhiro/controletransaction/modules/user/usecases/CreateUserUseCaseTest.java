package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.example.kazuhiro.controletransaction.exceptions.PasswordNotMatchesException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.CreateUserDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CreateUserUseCase createUserUseCase;

  private CreateUserDTO inputUser;

  @BeforeEach
  void setUp() {
    inputUser = CreateUserDTO.builder()
        .username("teste")
        .password("teste")
        .confirmPassword("teste")
        .limite(1000)
        .saldo(0)
        .build();
  }

  @Test
  @DisplayName("Deve criar um usuário com sucesso, criptografando a senha.")
  void shouldCreateUserWithSuccess() {
    String encryptedPassword = "senha_criptografada_123";
    UUID generatedId = UUID.randomUUID();

    UserEntity expectedSavedUser = UserEntity.builder()
        .id(generatedId)
        .username(inputUser.getUsername())
        .password(encryptedPassword)
        .limite(inputUser.getLimite())
        .saldo(inputUser.getSaldo())
        .build();

    when(userRepository.findByUsername(inputUser.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(inputUser.getPassword())).thenReturn(encryptedPassword);
    when(userRepository.save(any(UserEntity.class))).thenReturn(expectedSavedUser);

    UserEntity result = createUserUseCase.execute(inputUser);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(generatedId);
    assertThat(result.getPassword()).isEqualTo(encryptedPassword);

    verify(userRepository).findByUsername(inputUser.getUsername());
    verify(passwordEncoder).encode("teste");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar PasswordNotMatchesException quando as senhas não coincidirem.")
  void shouldThrowExceptionWhenPasswordsDoNotMatch() {
    inputUser.setConfirmPassword("senha_diferente");

    assertThatThrownBy(() -> createUserUseCase.execute(inputUser))
        .isInstanceOf(PasswordNotMatchesException.class);

    verify(userRepository, never()).findByUsername(anyString());
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserFoundException e interromper a criação se o username já existir.")
  void shouldThrowExceptionWhenUserAlreadyExists() {
    UserEntity existingUser = UserEntity.builder()
        .username(inputUser.getUsername())
        .build();

    when(userRepository.findByUsername(inputUser.getUsername())).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> createUserUseCase.execute(inputUser))
        .isInstanceOf(UserFoundException.class);

    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }
}
