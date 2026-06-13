package br.com.example.kazuhiro.controle_transaction.modules.transaction.useCases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import br.com.example.kazuhiro.controle_transaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controle_transaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controle_transaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controle_transaction.modules.user.service.UserBalanceService;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionUseCaseTest {

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
        .valor(1000)
        .descricao("Recebimento de salário")
        .build();
  }

  @Test
  @DisplayName("Deve criar transação quando o token e a URL forem do mesmo usuário e o tipo for válido")
  void shouldCreateTransactionWhenUserIdsMatchAndTypeIsValid() {
    UserEntity mockUser = UserEntity.builder().id(clientId).saldo(1000).limite(5000).build();

    // Configura o mock do serviço que atualiza o saldo
    when(userBalanceService.applyTransaction(any(UUID.class), any(String.class), any(Integer.class)))
        .thenReturn(mockUser);

    // Executa o caso de uso
    CreateTransactionResponseDTO response = createTransactionUseCase.execute(
        clientId.toString(),
        tokenId.toString(),
        requestDTO);

    // Asserções básicas
    assertThat(response).isNotNull();
    assertThat(response.getSaldo()).isEqualTo(1000);
    assertThat(response.getLimite()).isEqualTo(5000);

    // Verifica se salvou a transação no banco de dados
    verify(transactionRepository).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserIdNotMatchesException quando o id da URL não coincidir com o token")
  void shouldThrowWhenUserIdsDoNotMatch() {
    UUID differentTokenId = UUID.randomUUID();

    assertThatThrownBy(
        () -> createTransactionUseCase.execute(clientId.toString(), differentTokenId.toString(), requestDTO))
        .isInstanceOf(UserIdNotMatchesException.class);
  }

  @Test
  @DisplayName("Deve lançar IllegalTransactionTypeException quando o tipo for inválido")
  void shouldThrowWhenTransactionTypeIsInvalid() {
    requestDTO.setTipo("x");

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientId.toString(), tokenId.toString(), requestDTO))
        .isInstanceOf(IllegalTransactionTypeException.class);
  }

  @Test
  @DisplayName("Deve lançar LimitReachedException quando o saldo negativado ultrapassar o limite")
  void shouldPropagateUserBalanceServiceException() {
    when(userBalanceService.applyTransaction(clientId, requestDTO.getTipo(), requestDTO.getValor()))
        .thenThrow(new LimitReachedException());

    assertThatThrownBy(() -> createTransactionUseCase.execute(clientId.toString(), tokenId.toString(), requestDTO))
        .isInstanceOf(LimitReachedException.class);
  }
}
