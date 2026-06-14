package br.com.example.kazuhiro.controletransaction.modules.transaction.dtos;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record SaldoDTO(
    Long total,
    @JsonProperty("data_extrato") Instant dataExtrato,
    Long limite) {
}