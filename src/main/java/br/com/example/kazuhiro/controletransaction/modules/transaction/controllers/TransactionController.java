package br.com.example.kazuhiro.controletransaction.modules.transaction.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.kazuhiro.controletransaction.exceptions.IllegalTransactionTypeException;
import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotMatchesException;
import br.com.example.kazuhiro.controletransaction.modules.transaction.dtos.CreateTransactionRequestDTO;
import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;
import br.com.example.kazuhiro.controletransaction.modules.transaction.usecases.CreateTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
@Tag(name = "Transaction", description = "Controller responsável por criar transações para os usuários cadastrados.")
@PreAuthorize("hasRole('CLIENT')")
public class TransactionController {
  private final CreateTransactionUseCase createTransactionUseCase;

  @PostMapping("/{id}/transacoes")
  @PreAuthorize("hasRole('CLIENT')")
  @SecurityRequirement(name = "jwt_auth")
  @Operation(summary = "Cria uma nova transação para um cliente", description = "Registra uma transação de crédito ou débito no saldo do cliente e salva a transação no sistema.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Transação criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionEntity.class))),
      @ApiResponse(responseCode = "401", description = "Acesso não autorizado, token inválido ou ID divergente"),
      @ApiResponse(responseCode = "422", description = "Dados de transação inválidos, tipo incorreto ou limite atingido"),
      @ApiResponse(responseCode = "404", description = "ID do usuário não encontrado na base de dados"),
      @ApiResponse(responseCode = "500", description = "Erro interno ao processar a requisição")
  })
  public ResponseEntity<Object> createTransaction(
      HttpServletRequest request,
      @io.swagger.v3.oas.annotations.Parameter(description = "Dados da transação que será criada", required = true) @Valid @RequestBody CreateTransactionRequestDTO createTransactionRequestDTO,
      @io.swagger.v3.oas.annotations.Parameter(in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH, description = "ID do cliente que receberá a transação", required = true) @PathVariable("id") String clientId) {

    Object clientAttribute = request.getAttribute("cliente_id");
    if (clientAttribute == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT ausente, expirado ou inválido.");
    }
    String clientIdStr = clientAttribute.toString();

    try {
      var transaction = this.createTransactionUseCase.execute(clientId, clientIdStr, createTransactionRequestDTO);
      return ResponseEntity.ok().body(transaction);
    } catch (UserIdNotFoundException | ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (LimitReachedException | IllegalTransactionTypeException e) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
    } catch (UserIdNotMatchesException | AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar a requisição.");
    }
  }
}
