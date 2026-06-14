package br.com.example.kazuhiro.controletransaction.modules.transaction.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

import java.util.List;

@Builder
public record ExtratoResponseDTO(
        SaldoDTO saldo,
        @JsonProperty("ultimas_transacoes") List<TransacaoDTO> ultimasTransacoes) {
}
