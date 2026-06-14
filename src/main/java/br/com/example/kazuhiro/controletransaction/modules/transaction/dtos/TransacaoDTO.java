package br.com.example.kazuhiro.controletransaction.modules.transaction.dtos;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record TransacaoDTO(
    Long valor,
    char tipo,
    String descricao,
    @JsonProperty("realizada_em") Instant realizadaEm) {
}