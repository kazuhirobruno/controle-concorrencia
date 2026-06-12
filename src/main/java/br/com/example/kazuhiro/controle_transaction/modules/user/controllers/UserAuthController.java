package br.com.example.kazuhiro.controle_transaction.modules.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.useCases.AuthUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Controller para realizar autenticação de usuário.")
public class UserAuthController {
  @Autowired
  private AuthUserUseCase authUserUseCase;

  @PostMapping("/auth")
  @Operation(summary = "Autenticar cliente", description = "Função responsável por realizar a autenticação do cliente.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso", content = {
          @Content(schema = @Schema(implementation = AuthUserResponseDTO.class))
      }),
      @ApiResponse(responseCode = "401", description = "Erro durante a autenticação - credenciais inválidas ou erro no processo.")
  })
  public ResponseEntity<Object> auth(@RequestBody AuthUserRequestDTO authUserRequestDTO) {
    try {
      var token = this.authUserUseCase.execute(authUserRequestDTO);
      return ResponseEntity.ok().body(token);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
  }
}
