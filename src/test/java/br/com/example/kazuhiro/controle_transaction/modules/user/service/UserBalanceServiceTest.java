package br.com.example.kazuhiro.controle_transaction.modules.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import br.com.example.kazuhiro.controle_transaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controle_transaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserBalanceServiceTest {

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
        .limite(1000)
        .saldo(0)
        .build();
  }

  @Test
  @DisplayName("Deve aplicar crédito quando o tipo for c e salvar o usuário")
  void shouldApplyCreditTransaction() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = userBalanceService.applyTransaction(userId, "c", 500);

    assertThat(result.getSaldo()).isEqualTo(500);
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve aplicar débito quando o tipo for d e salvar o usuário")
  void shouldApplyDebitTransaction() {
    existingUser.setSaldo(800);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity result = userBalanceService.applyTransaction(userId, "d", 300);

    assertThat(result.getSaldo()).isEqualTo(500);
    verify(userRepository).save(existingUser);
  }

  @Test
  @DisplayName("Deve lançar UserIdNotFoundException quando o usuário não existir")
  void shouldThrowWhenUserNotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userBalanceService.applyTransaction(userId, "c", 100))
        .isInstanceOf(UserIdNotFoundException.class);
  }

  @Test
  @DisplayName("Deve lançar LimitReachedException quando o débito ultrapassar o limite negativo")
  void shouldThrowWhenDebitExceedsLimit() {
    existingUser.setSaldo(0);
    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> userBalanceService.applyTransaction(userId, "d", 1500))
        .isInstanceOf(LimitReachedException.class);
  }
}
