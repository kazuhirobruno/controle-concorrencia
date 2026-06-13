package br.com.example.kazuhiro.controletransaction.modules.user.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.kazuhiro.controletransaction.exceptions.PasswordNotMatchesException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.CreateUserDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.ChangeUserPasswordUseCase;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.CreateUserUseCase;
import br.com.example.kazuhiro.controletransaction.modules.user.usecases.DeleteUserUseCase;

import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Controller para CRUD de clientes")
public class UserController {

  private final CreateUserUseCase createUserUseCase;
  private final ChangeUserPasswordUseCase changeUserPasswordUseCase;
  private final DeleteUserUseCase deleteUserUseCase;

  @PostMapping("/")
  @Operation(summary = "Cadastrar novo cliente", description = "Função responsável por realizar o cadastro do cliente na base de dados.")
  @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso.", content = {
      @Content(schema = @Schema(implementation = UserEntity.class))
  })
  @ApiResponse(responseCode = "400", description = "Dados inválidos, senhas não coincidem ou username existente.")
  public ResponseEntity<Object> create(@Valid @RequestBody CreateUserDTO createUserDTO) {
    try {
      var result = this.createUserUseCase.execute(createUserDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(result);
    } catch (PasswordNotMatchesException | UserFoundException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Erro ao processar o cadastro.");
    }
  }

  @PatchMapping("/password")
  @Operation(summary = "Alterar senha do cliente logado", description = "Atualiza a senha do usuário atualmente autenticado no token JWT.")
  @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso.")
  @ApiResponse(responseCode = "400", description = "Dados inválidos ou senhas não coincidem.")
  @ApiResponse(responseCode = "401", description = "Token JWT ausente, expirado ou inválido.")
  @ApiResponse(responseCode = "404", description = "Recurso indisponível ou inexistente.")
  @PreAuthorize("hasRole('CLIENT')")
  @SecurityRequirement(name = "jwt_auth")
  public ResponseEntity<Object> changePassword(HttpServletRequest request,
      @Valid @RequestBody ChangeUserPasswordDTO changeUserPasswordDTO) {
    try {
      String clientIdStr = request.getAttribute("cliente_id").toString();
      UUID clientId = UUID.fromString(clientIdStr);

      this.changeUserPasswordUseCase.execute(changeUserPasswordDTO, clientId);

      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getLocalizedMessage());
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar a requisição.");
    }
  }

  @DeleteMapping("/")
  @Operation(summary = "Deletar conta do cliente logado", description = "Exclui permanentemente a conta do usuário atualmente autenticado no token JWT.")
  @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso.")
  @ApiResponse(responseCode = "401", description = "Token JWT ausente, expirado ou inválido.")
  @ApiResponse(responseCode = "404", description = "Recurso indisponível ou inexistente.")
  @PreAuthorize("hasRole('CLIENT')")
  @SecurityRequirement(name = "jwt_auth")
  public ResponseEntity<Object> deleteUser(HttpServletRequest request) {
    try {
      String clientIdStr = request.getAttribute("cliente_id").toString();
      UUID clientId = UUID.fromString(clientIdStr);

      this.deleteUserUseCase.execute(clientId);
      return ResponseEntity.noContent().build();
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT ausente, expirado ou inválido.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar a requisição.");
    }
  }
}
