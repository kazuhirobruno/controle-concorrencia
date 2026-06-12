package br.com.example.kazuhiro.controle_transaction.modules.user.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.ChangeUserPasswordUseCase;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.CreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Controller para CRUD de clientes")
public class UserController {

  @Autowired
  private CreateUserUseCase createUserUseCase;

  @Autowired
  private ChangeUserPasswordUseCase changeUserPasswordUseCase;

  @PostMapping("/")
  @Operation(summary = "Cadastrar novo cliente", description = "Função responsável por realizar o cadastro do cliente na base de dados.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", content = {
          @Content(array = @ArraySchema(schema = @Schema(implementation = UserEntity.class)))
      }),
      @ApiResponse(responseCode = "400", description = "Falha ao cadastrar novo usuário.")
  })
  public ResponseEntity<Object> create(@Valid @RequestBody UserEntity userEntity) {
    try {
      var result = this.createUserUseCase.execute(userEntity);
      return ResponseEntity.ok().body(result);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PatchMapping("/password")
  @Operation(summary = "Alterar senha do cliente logado", description = "Atualiza a senha do usuário atualmente autenticado no token JWT.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso."),
      @ApiResponse(responseCode = "400", description = "Dados inválidos ou senhas não coincidem."),
      @ApiResponse(responseCode = "401", description = "Token JWT ausente, expirado ou inválido.")
  })
  @SecurityRequirement(name = "jwt_auth")
  public ResponseEntity<Object> changePassword(HttpServletRequest request,
      @Valid @RequestBody ChangeUserPasswordDTO changeUserPasswordDTO) {
    try {
      String clientIdStr = request.getAttribute("cliente_id").toString();
      UUID clientId = UUID.fromString(clientIdStr);

      this.changeUserPasswordUseCase.execute(changeUserPasswordDTO, clientId);

      return ResponseEntity.ok("Senha alterada com sucesso.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar a requisição.");
    }
  }
}
