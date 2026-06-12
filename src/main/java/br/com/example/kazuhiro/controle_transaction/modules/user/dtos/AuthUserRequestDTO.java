package br.com.example.kazuhiro.controle_transaction.modules.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de requisição para autenticação do usuário.")
public class AuthUserRequestDTO {

  @Schema(name = "username", description = "Nome de usuário utilizado no login.", requiredMode = RequiredMode.REQUIRED, example = "kazuhiro")
  private String username;

  @Schema(name = "password", description = "Senha do usuário.", requiredMode = RequiredMode.REQUIRED, example = "Senha@123")
  private String password;
}