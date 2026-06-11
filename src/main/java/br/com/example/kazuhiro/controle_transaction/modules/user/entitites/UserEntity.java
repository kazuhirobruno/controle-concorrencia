package br.com.example.kazuhiro.controle_transaction.modules.user.entitites;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "client")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Length(max = 20)
  @Schema(name = "username", description = "Nome de usuário para o login.", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Length(max = 128)
  @Schema(name = "password", description = "Senha do usuário.", requiredMode = RequiredMode.REQUIRED)
  private String password;

  @PositiveOrZero
  @Schema(name = "limite", description = "Limite do usuário.", requiredMode = RequiredMode.REQUIRED)
  private Integer limite;

  @Schema(name = "saldo", description = "Saldo atual do usuário.", requiredMode = RequiredMode.REQUIRED)
  private Integer saldo;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
