package br.com.example.kazuhiro.controletransaction.modules.user.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de resposta retornado após uma autenticação bem-sucedida.")
public class AuthUserResponseDTO {

  @JsonProperty("access_token")
  @Schema(description = "Token de acesso no formato JWT utilizado para autenticar as requisições protegidas.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @JsonProperty("expires_in")
  @Schema(description = "Tempo de expiração do token em milissegundos a partir da data de emissão.", example = "3600000")
  private Long expiresIn;

  @Schema(description = "Lista de permissões e papéis atribuídos ao usuário autenticado.", example = "[\"ROLE_CLIENT\", \"ROLE_ADMIN\"]")
  private List<String> roles;
}