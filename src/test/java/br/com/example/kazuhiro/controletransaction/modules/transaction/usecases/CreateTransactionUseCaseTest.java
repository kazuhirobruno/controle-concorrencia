package br.com.example.kazuhiro.controletransaction.modules.transaction.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.example.kazuhiro.controletransaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controletransaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controletransaction.modules.user.service.UserBalanceService;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {
  @Mock
  private UserBalanceService userBalanceService;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private CreateTransactionUseCase createTransactionUseCase;

  private CreateTransactionRequestDTO requestDTO;
  private UUID clientId;
  private UUID tokenId;

  @BeforeEach
  void setUp() {
    clientId = UUID.randomUUID();
    tokenId = clientId;
    requestDTO = CreateTransactionRequestDTO.builder()
        .tipo("c")
        .valor(1000L)
        .descricao("Recebimento de salário")
        .build();
  }

  @Test
  @DisplayName("Deve criar transação quando o token e a URL forem do mesmo usuário e o tipo for válido")
  void shouldCreateTransactionWhenUserIdsMatchAndTypeIsValid() {
    UserEntity mockUser = UserEntity.builder().id(clientId).saldo(1000L).limite(5000L).active(true).build();
    String clientIdStr = clientId.toString();
    String tokenIdStr = tokenId.toString();

    when(userBalanceService.applyTransaction(any(UUID.class), anyString(), anyLong()))
        .thenReturn(mockUser);

    CreateTransactionResponseDTO response = createTransactionUseCase.execute(
        clientIdStr,
        tokenIdStr,
        requestDTO);

    assertThat(response).isNotNull();
    assertThat(response.getSaldo()).isEqualTo(1000L);
    assertThat(response.getLimite()).isEqualTo(5000L);

    verify(transactionRepository).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve criar transação com sucesso quando o tipo for débito para cobrir todas as ramificações lógicas")
  void shouldCreateTransactionSuccessfullyWhenTypeIsDebit() {
    requestDTO.setTipo("d");
    UserEntity mockUser = UserEntity.builder().id(clientId).saldo(500L).limite(5000L).active(true).build();
    String clientIdStr = clientId.toString();
    String tokenIdStr = tokenId.toString();

    when(userBalanceService.applyTransaction(any(UUID.class), anyString(), anyLong()))
        .thenReturn(mockUser);

    CreateTransactionResponseDTO response = createTransactionUseCase.execute(
        clientIdStr,
        tokenIdStr,
        requestDTO);

    assertThat(response).isNotNull();
    assertThat(response.getSaldo()).isEqualTo(500L);
    verify(transactionRepository).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserIdNotMatchesException quando o id da URL não coincidir com o token")
  void shouldThrowWhenUserIdsDoNotMatch() {
    UUID differentTokenId = UUID.randomUUID();
    String clientIdStr = clientId.toString();
    String differentTokenIdStr = differentTokenId.toString();

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientIdStr, differentTokenIdStr, requestDTO))
        .isInstanceOf(UserIdNotMatchesException.class);

    verify(userBalanceService, never()).applyTransaction(any(UUID.class), anyString(), anyLong());
    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve lançar IllegalTransactionTypeException quando o tipo for inválido")
  void shouldThrowWhenTransactionTypeIsInvalid() {
    requestDTO.setTipo("x");
    String clientIdStr = clientId.toString();
    String tokenIdStr = tokenId.toString();

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientIdStr, tokenIdStr, requestDTO))
        .isInstanceOf(IllegalTransactionTypeException.class);

    verify(userBalanceService, never()).applyTransaction(any(UUID.class), anyString(), anyLong());
    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve propagar a exceção do UserBalanceService quando o limite for atingido")
  void shouldPropagateUserBalanceServiceException() {
    String clientIdStr = clientId.toString();
    String tokenIdStr = tokenId.toString();

    when(userBalanceService.applyTransaction(any(UUID.class), anyString(), anyLong()))
        .thenThrow(new LimitReachedException());

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientIdStr, tokenIdStr, requestDTO))
        .isInstanceOf(LimitReachedException.class);

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve propagar a exceção do UserBalanceService quando o usuário estiver inativo")
  void shouldPropagateResourceNotFoundExceptionWhenUserIsInactive() {
    String clientIdStr = clientId.toString();
    String tokenIdStr = tokenId.toString();

    when(userBalanceService.applyTransaction(any(UUID.class), anyString(), anyLong()))
        .thenThrow(new ResourceNotFoundException("Recurso não encontrado."));

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientIdStr, tokenIdStr, requestDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Recurso não encontrado.");

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }
}
