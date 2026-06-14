package br.com.example.kazuhiro.controletransaction.modules.transaction.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.ExtratoResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controletransaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.service.UserBalanceServiceInterface;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade do Use Case GetExtratoUseCase")
class GetExtratoUseCaseTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private UserBalanceServiceInterface userBalanceService;

  @InjectMocks
  private GetExtratoUseCase getExtratoUseCase;

  private UUID clientUuid;
  private String clientIdStr;
  private UserEntity mockUser;

  @BeforeEach
  void setUp() {
    clientUuid = UUID.randomUUID();
    clientIdStr = clientUuid.toString();

    mockUser = UserEntity.builder()
        .id(clientUuid)
        .limite(100000L)
        .saldo(-9098L)
        .active(true)
        .build();
  }

  @Test
  @DisplayName("Deve retornar o extrato montado com sucesso quando os IDs forem iguais")
  void shouldReturnExtratoSuccessfully() {
    LocalDateTime dataFixa2060 = LocalDateTime.parse("2060-01-17T00:00:00");

    TransactionEntity mockTransaction = new TransactionEntity();
    mockTransaction.setValor(10L);
    mockTransaction.setTipo("c");
    mockTransaction.setDescricao("descricao");
    mockTransaction.setRealizadaEm(dataFixa2060);

    when(userBalanceService.findActiveUserById(clientUuid)).thenReturn(mockUser);
    when(transactionRepository.findTop10ByClienteIdOrderByRealizadaEmDesc(clientUuid))
        .thenReturn(List.of(mockTransaction));

    ExtratoResponseDTO result = getExtratoUseCase.execute(clientIdStr, clientIdStr);

    assertThat(result).isNotNull();
    assertThat(result.saldo()).isNotNull();
    assertThat(result.saldo().limite()).isEqualTo(100000L);
    assertThat(result.saldo().total()).isEqualTo(-9098L);
    assertThat(result.saldo().dataExtrato()).isNotNull();

    assertThat(result.ultimasTransacoes()).hasSize(1);
    assertThat(result.ultimasTransacoes().get(0).valor()).isEqualTo(10L);
    assertThat(result.ultimasTransacoes().get(0).tipo()).isEqualTo('c');
    assertThat(result.ultimasTransacoes().get(0).descricao()).isEqualTo("descricao");
    assertThat(result.ultimasTransacoes().get(0).realizadaEm()).isNotNull();

    verify(userBalanceService).findActiveUserById(clientUuid);
    verify(transactionRepository).findTop10ByClienteIdOrderByRealizadaEmDesc(clientUuid);
  }

  @Test
  @DisplayName("Deve retornar o extrato com a lista vazia caso o cliente não tenha transações")
  void shouldReturnExtratoWithEmptyTransactionsList() {
    when(userBalanceService.findActiveUserById(clientUuid)).thenReturn(mockUser);
    when(transactionRepository.findTop10ByClienteIdOrderByRealizadaEmDesc(clientUuid))
        .thenReturn(Collections.emptyList());

    ExtratoResponseDTO result = getExtratoUseCase.execute(clientIdStr, clientIdStr);

    assertThat(result).isNotNull();
    assertThat(result.ultimasTransacoes()).isEmpty();
  }

  @Test
  @DisplayName("Deve lançar UserIdNotMatchesException quando o ID do cliente for diferente do ID do token")
  void shouldThrowUserIdNotMatchesExceptionWhenIdsDoNotMatch() {
    String alternativeTokenId = UUID.randomUUID().toString();

    assertThatThrownBy(() -> getExtratoUseCase.execute(clientIdStr, alternativeTokenId))
        .isInstanceOf(UserIdNotMatchesException.class);

    verifyNoInteractions(userBalanceService);
    verifyNoInteractions(transactionRepository);
  }
}