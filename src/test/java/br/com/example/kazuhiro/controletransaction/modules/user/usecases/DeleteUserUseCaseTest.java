package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private DeleteUserUseCase deleteUserUseCase;

  private UUID mockUserId;
  private UserEntity mockUser;

  @BeforeEach
  void setUp() {
    mockUserId = UUID.randomUUID();

    mockUser = UserEntity.builder()
        .id(mockUserId)
        .username("original_user")
        .password("password123")
        .active(true)
        .build();
  }

  @Test
  @DisplayName("Deve inativar e anonimizar um usuário com sucesso quando o ID existir.")
  void shouldDeleteUserWithSuccess() {
    when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

    deleteUserUseCase.execute(mockUserId);

    verify(userRepository, times(1)).findById(mockUserId);
    verify(userRepository, times(1)).save(mockUser);

    Assertions.assertThat(mockUser.isActive()).isFalse();
    Assertions.assertThat(mockUser.getUsername())
        .startsWith("deleted_")
        .hasSizeLessThanOrEqualTo(50);
  }

  @Test
  @DisplayName("Deve lançar UserIdNotFoundException quando o ID do usuário não for encontrado.")
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> deleteUserUseCase.execute(mockUserId))
        .isInstanceOf(UserIdNotFoundException.class);

    verify(userRepository, times(1)).findById(mockUserId);
    verify(userRepository, never()).save(any(UserEntity.class));
  }
}