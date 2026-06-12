package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.example.kazuhiro.controle_transaction.exceptions.UserFoundException;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CreateUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CreateUserUseCase createUserUseCase;

  private UserEntity inputUser;

  @BeforeEach
  void setUp() {
    inputUser = UserEntity.builder()
        .username("teste")
        .password("teste")
        .limite(1000)
        .saldo(0)
        .build();
  }

  @Test
  @DisplayName("Deve criar um usuário com sucesso, criptografando a senha.")
  void shouldCreateUserWithSuccess() {
    // Arrange (Organização)
    String encryptedPassword = "senha_criptografada_123";
    UserEntity savedUser = UserEntity.builder()
        .id(UUID.randomUUID())
        .username(inputUser.getUsername())
        .password(encryptedPassword)
        .limite(inputUser.getLimite())
        .saldo(inputUser.getSaldo())
        .build();

    when(userRepository.findByUsername(inputUser.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(inputUser.getPassword())).thenReturn(encryptedPassword);
    when(userRepository.save(ArgumentMatchers.any(UserEntity.class))).thenReturn(savedUser);

    UserEntity result = createUserUseCase.execute(inputUser);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getPassword()).isEqualTo(encryptedPassword);

    verify(userRepository).findByUsername(inputUser.getUsername());
    verify(passwordEncoder).encode("teste");
    verify(userRepository).save(inputUser);
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

    verify(passwordEncoder, never()).encode(ArgumentMatchers.anyString());
    verify(userRepository, never()).save(ArgumentMatchers.any(UserEntity.class));
  }
}
