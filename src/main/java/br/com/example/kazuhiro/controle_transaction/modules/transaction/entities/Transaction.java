package br.com.example.kazuhiro.controle_transaction.modules.transaction.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import br.com.example.kazuhiro.controle_transaction.modules.transaction.types.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Entity(name = "transaction")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Schema(example = "c", description = "Indica o tipo de transação. Enviar apenas 'c' ou 'd' como valor.", requiredMode = RequiredMode.REQUIRED)
  private TransactionType tipo;

  @PositiveOrZero
  @Schema(example = "1000", description = "Valor a ser debitado ou creditado.", requiredMode = RequiredMode.REQUIRED)
  private int valor;

  @NotBlank
  @Length(max = 250)
  @Schema(example = "Valor a receber", description = "Descreve o motivo da transação.", requiredMode = RequiredMode.REQUIRED)
  private String descricao;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
