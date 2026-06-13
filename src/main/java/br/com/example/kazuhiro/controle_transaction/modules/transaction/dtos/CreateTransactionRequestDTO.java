package br.com.example.kazuhiro.controle_transaction.modules.transaction.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de requisição para a criação de uma nova transação.")
public class CreateTransactionRequestDTO {

  @NotNull(message = "O tipo de transação não pode ser nulo.")
  @JsonProperty("tipo")
  @Schema(name = "tipo", example = "c", description = "Indica o tipo de transação. Enviar apenas 'c' para crédito ou 'd' para débito.", requiredMode = RequiredMode.REQUIRED)
  private String tipo;

  @PositiveOrZero(message = "O valor da transação deve ser igual ou maior que zero.")
  @JsonProperty("valor")
  @Schema(name = "valor", example = "1000", description = "Valor a ser debitado ou creditado.", requiredMode = RequiredMode.REQUIRED)
  private int valor;

  @NotBlank(message = "A descrição não pode estar em branco.")
  @Length(min = 1, max = 10, message = "A descrição deve conter entre 1 e 10 caracteres.")
  @JsonProperty("descricao")
  @Schema(name = "descricao", example = "Valor a receber", description = "Descreve o motivo da transação.", requiredMode = RequiredMode.REQUIRED)
  private String descricao;
}
