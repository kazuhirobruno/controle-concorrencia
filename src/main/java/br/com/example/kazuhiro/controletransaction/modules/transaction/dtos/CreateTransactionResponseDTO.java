package br.com.example.kazuhiro.controletransaction.modules.transaction.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de resposta para a criação de uma nova transação.")
public class CreateTransactionResponseDTO {

  @Schema(example = "1000", description = "Limite do usuário.")
  private int limite;

  @Schema(example = "1000", description = "Saldo do usuário.")
  private int saldo;
}
