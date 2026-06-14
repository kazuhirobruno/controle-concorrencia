package br.com.example.kazuhiro.controletransaction.modules.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserBalanceServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserBalanceService userBalanceService;

  private UUID userId;
  private UserEntity existingUser;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    existingUser = UserEntity.builder()
        .id(userId)
        .username("cliente")
        .password("senha")
        .limite(1000L)
        .saldo(0L)
        .active(true)
        .build();
  }

  @Test
  @DisplayName("Deve aplicar crédito quando o tipo for c e salvar o usuário")
  void shouldApplyCreditTransaction() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = userBalanceService.applyTransaction(userId, "c", 500L);

    assertThat(result.getSaldo()).isEqualTo(500L);
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve aplicar débito quando o tipo for d e salvar o usuário")
  void shouldApplyDebitTransaction() {
    existingUser.setSaldo(800L);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = userBalanceService.applyTransaction(userId, "d", 300L);

    assertThat(result.getSaldo()).isEqualTo(500L);
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve permitir débito que atinja exatamente o limite negativo permitido")
  void shouldAllowDebitWhenItHitsExactLimit() {
    existingUser.setSaldo(0L);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = userBalanceService.applyTransaction(userId, "d", 1000L);

    assertThat(result.getSaldo()).isEqualTo(-1000L);
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve lançar UserIdNotFoundException quando o usuário não existir")
  void shouldThrowWhenUserNotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userBalanceService.applyTransaction(userId, "c", 100L))
        .isInstanceOf(UserIdNotFoundException.class);

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando o usuário encontrado estiver inativo")
  void shouldThrowWhenUserIsInactive() {
    existingUser.setActive(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> userBalanceService.applyTransaction(userId, "c", 100L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Recurso não encontrado.");

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar LimitReachedException quando o débito ultrapassar o limite negativo")
  void shouldThrowWhenDebitExceedsLimit() {
    existingUser.setSaldo(0L);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> userBalanceService.applyTransaction(userId, "d", 1500L))
        .isInstanceOf(LimitReachedException.class);

    verify(userRepository, never()).save(any(UserEntity.class));
  }
}
