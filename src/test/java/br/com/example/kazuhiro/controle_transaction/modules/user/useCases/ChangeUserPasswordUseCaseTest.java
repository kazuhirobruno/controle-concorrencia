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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ChangeUserPasswordUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ChangeUserPasswordUseCase changeUserPasswordUseCase;

  private UUID clientId;
  private UserEntity existingUser;

  @BeforeEach
  void setUp() {
    clientId = UUID.randomUUID();

    existingUser = UserEntity.builder()
        .id(clientId)
        .username("teste")
        .password("teste")
        .build();
  }

  @Test
  @DisplayName("Deve alterar a senha com sucesso quando os dados forem válidos e o usuário existir.")
  void shouldChangePasswordWithSuccess() {
    ChangeUserPasswordDTO validDto = ChangeUserPasswordDTO.builder()
        .newPassword("novaSenha123")
        .confirmNewPassword("novaSenha123")
        .build();

    String encryptedPasswordMock = "nova_senha_criptografada_hash";

    when(userRepository.findById(clientId)).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.encode(validDto.getNewPassword())).thenReturn(encryptedPasswordMock);
    when(userRepository.save(ArgumentMatchers.any(UserEntity.class))).thenReturn(existingUser);

    changeUserPasswordUseCase.execute(validDto, clientId);

    assertThat(existingUser.getPassword()).isEqualTo(encryptedPasswordMock);

    verify(userRepository).findById(clientId);
    verify(passwordEncoder).encode("novaSenha123");
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve lançar IllegalArgumentException quando a nova senha e a confirmação forem diferentes.")
  void shouldThrowExceptionWhenPasswordsDoNotMatch() {
    ChangeUserPasswordDTO invalidDto = ChangeUserPasswordDTO.builder()
        .newPassword("novaSenha123")
        .confirmNewPassword("senhaDiferente456")
        .build();

    assertThatThrownBy(() -> changeUserPasswordUseCase.execute(invalidDto, clientId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("A nova senha e a confirmação de senha não coincidem.");

    verify(userRepository, never()).findById(ArgumentMatchers.any(UUID.class));
    verify(passwordEncoder, never()).encode(ArgumentMatchers.anyString());
    verify(userRepository, never()).save(ArgumentMatchers.any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UsernameNotFoundException quando o ID do cliente não for encontrado no repositório.")
  void shouldThrowExceptionWhenUserNotFound() {
    ChangeUserPasswordDTO validDto = ChangeUserPasswordDTO.builder()
        .newPassword("novaSenha123")
        .confirmNewPassword("novaSenha123")
        .build();

    when(userRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> changeUserPasswordUseCase.execute(validDto, clientId))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Usuário não encontrado.");

    verify(passwordEncoder, never()).encode(ArgumentMatchers.anyString());
    verify(userRepository, never()).save(ArgumentMatchers.any(UserEntity.class));
  }
}