package br.com.example.kazuhiro.controle_transaction.modules.transaction.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

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
  private TransactionType tipo;

  @PositiveOrZero
  private int valor;

  @NotBlank
  @Length(max = 250)
  private String descricao;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
