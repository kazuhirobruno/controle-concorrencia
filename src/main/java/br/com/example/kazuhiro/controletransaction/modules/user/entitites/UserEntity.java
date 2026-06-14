package br.com.example.kazuhiro.controletransaction.modules.user.entitites;

import java.time.Instant;
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
  @Length(max = 50)
  @Schema(name = "username", description = "Nome de usuário para o login.", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Length(max = 128, message = "A nova senha deve conter até 128 caracteres.")
  @Schema(name = "password", description = "Senha do usuário.", requiredMode = RequiredMode.REQUIRED)
  private String password;

  @PositiveOrZero
  @Schema(name = "limite", description = "Limite do usuário.", requiredMode = RequiredMode.REQUIRED)
  private Long limite;

  @Schema(name = "saldo", description = "Saldo atual do usuário.", requiredMode = RequiredMode.REQUIRED)
  private Long saldo;

  @Schema(name = "active", description = "Indica se o usuário está ativo no sistema.", defaultValue = "true", requiredMode = RequiredMode.REQUIRED)
  private boolean active;

  @CreationTimestamp
  private Instant createdAt;
}
