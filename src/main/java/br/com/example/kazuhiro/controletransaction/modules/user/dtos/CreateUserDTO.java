package br.com.example.kazuhiro.controletransaction.modules.user.dtos;

import org.hibernate.validator.constraints.Length;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
public class CreateUserDTO {

  @NotBlank(message = "O nome de usuário é obrigatório.")
  @Length(max = 20, message = "O nome de usuário deve conter até 20 caracteres.")
  @Schema(name = "username", description = "Nome de usuário para o login.", requiredMode = RequiredMode.REQUIRED, example = "kazuhiro.dev")
  private String username;

  @NotBlank(message = "A senha é obrigatória.")
  @Length(max = 128, message = "A senha deve conter até 128 caracteres.")
  @Schema(name = "password", description = "Senha do usuário.", requiredMode = RequiredMode.REQUIRED, example = "SenhaSegura123")
  private String password;

  @NotBlank(message = "A confirmação de senha é obrigatória.")
  @Length(max = 128, message = "A confirmação de senha deve conter até 128 caracteres.")
  @Schema(name = "confirmPassword", description = "Confirmação da senha digitada acima. Deve ser idêntica.", requiredMode = RequiredMode.REQUIRED, example = "SenhaSegura123")
  private String confirmPassword;

  @PositiveOrZero(message = "O limite não pode ser um valor negativo.")
  @Schema(name = "limite", description = "Limite de crédito inicial do usuário em centavos.", requiredMode = RequiredMode.REQUIRED, example = "100000")
  private Integer limite;

  @Schema(name = "saldo", description = "Saldo inicial da conta do usuário em centavos.", requiredMode = RequiredMode.REQUIRED, example = "0")
  private Integer saldo;
}