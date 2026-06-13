package br.com.example.kazuhiro.controletransaction.modules.user.dtos;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de requisição para redefinição de senha com validação de confirmação.")
public class ChangeUserPasswordDTO {

  @NotBlank(message = "A nova senha não pode estar em branco.")
  @Length(max = 128, message = "A nova senha deve conter até 128 caracteres.")
  @JsonProperty("new_password")
  @Schema(description = "Nova senha que será registrada para o usuário.", requiredMode = RequiredMode.REQUIRED, example = "Nova@123")
  private String newPassword;

  @NotBlank(message = "A confirmação da nova senha não pode estar em branco.")
  @JsonProperty("confirm_new_password")
  @Schema(description = "Confirmação da nova senha. Deve ser exatamente igual ao campo new_password.", requiredMode = RequiredMode.REQUIRED, example = "Nova@123")
  private String confirmNewPassword;
}