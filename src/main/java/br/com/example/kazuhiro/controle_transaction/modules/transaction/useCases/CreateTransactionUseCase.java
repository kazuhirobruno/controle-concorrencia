package br.com.example.kazuhiro.controle_transaction.modules.transaction.useCases;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controle_transaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controle_transaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos.CreateTransactionResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controle_transaction.modules.user.service.UserBalanceService;

@Service
public class CreateTransactionUseCase {

  @Autowired
  private UserBalanceService userBalanceService;

  @Autowired
  private TransactionRepository transactionRepository;

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

    var response = CreateTransactionResponseDTO.builder()
        .limite(updatedUser.getLimite())
        .saldo(updatedUser.getSaldo())
        .build();
    return response;
  }
}
