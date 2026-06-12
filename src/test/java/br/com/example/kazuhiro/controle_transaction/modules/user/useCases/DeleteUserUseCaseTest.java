package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private DeleteUserUseCase deleteUserUseCase;

  private UUID mockUserId;

  @BeforeEach
  void setUp() {
    mockUserId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Deve deletar um usuário com sucesso quando o ID existir.")
  void shouldDeleteUserWithSuccess() {
    when(userRepository.existsById(mockUserId)).thenReturn(true);

    deleteUserUseCase.execute(mockUserId);

    verify(userRepository).existsById(mockUserId);
    verify(userRepository).deleteById(mockUserId);
  }

  @Test
  @DisplayName("Deve lançar UsernameNotFoundException quando o ID do usuário não for encontrado.")
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.existsById(mockUserId)).thenReturn(false);

    Assertions.assertThatThrownBy(() -> deleteUserUseCase.execute(mockUserId))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Usuário não encontrado.");

    verify(userRepository).existsById(mockUserId);
    verify(userRepository, never()).deleteById(any(UUID.class));
  }
}