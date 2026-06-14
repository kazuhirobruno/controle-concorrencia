package br.com.example.kazuhiro.controletransaction.modules.transaction.usecases;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.ExtratoResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.SaldoDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.TransacaoDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.repository.TransactionRepository;
import br.com.example.kazuhiro.controletransaction.modules.user.service.UserBalanceServiceInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExtratoUseCase {

  private final TransactionRepository transactionRepository;
  private final UserBalanceServiceInterface userBalanceService;

  @Transactional(readOnly = true)
  public ExtratoResponseDTO execute(String clientId, String tokenUserId) {

    if (!clientId.equals(tokenUserId)) {
      throw new UserIdNotMatchesException();
    }

    UUID clientUuid = UUID.fromString(clientId);
    var user = userBalanceService.findActiveUserById(clientUuid);
    var transacoesBanco = this.transactionRepository.findTop10ByClienteIdOrderByRealizadaEmDesc(clientUuid);
    SaldoDTO saldoDTO = SaldoDTO.builder().limite(user.getLimite()).total(user.getSaldo()).dataExtrato(Instant.now())
        .build();

    List<TransacaoDTO> transacoesDTO = transacoesBanco.stream()
        .map(t -> TransacaoDTO.builder()
            .valor(t.getValor())
            .tipo(t.getTipo().toString().charAt(0))
            .descricao(t.getDescricao())
            .realizadaEm(t.getRealizadaEm())
            .build())
        .collect(Collectors.toList());

    return new ExtratoResponseDTO(saldoDTO, transacoesDTO);
  }
}