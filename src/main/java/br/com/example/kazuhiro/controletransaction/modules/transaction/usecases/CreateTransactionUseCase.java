package br.com.example.kazuhiro.controletransaction.modules.transaction.usecases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controletransaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controletransaction.modules.user.service.UserBalanceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateTransactionUseCase {
  private final UserBalanceService userBalanceService;
  private final TransactionRepository transactionRepository;

  @Transactional
  public CreateTransactionResponseDTO execute(String urlUserId, String tokenUserId,
      CreateTransactionRequestDTO createTransactionRequestDTO) {
    String transactionType = createTransactionRequestDTO.getTipo();
    if (!urlUserId.equals(tokenUserId)) {
      throw new UserIdNotMatchesException();
    }

    if (!transactionType.equalsIgnoreCase("c") && !transactionType.equalsIgnoreCase("d")) {
      throw new IllegalTransactionTypeException(transactionType);
    }

    UUID tokenId = UUID.fromString(tokenUserId);

    var updatedUser = this.userBalanceService.applyTransaction(tokenId, createTransactionRequestDTO.getTipo(),
        createTransactionRequestDTO.getValor());

    TransactionEntity entity = TransactionEntity.builder()
        .tipo(createTransactionRequestDTO.getTipo())
        .descricao(createTransactionRequestDTO.getDescricao())
        .valor(createTransactionRequestDTO.getValor())
        .user(updatedUser).build();

    this.transactionRepository.save(entity);

    return CreateTransactionResponseDTO.builder()
        .limite(updatedUser.getLimite())
        .saldo(updatedUser.getSaldo())
        .build();
  }
}
