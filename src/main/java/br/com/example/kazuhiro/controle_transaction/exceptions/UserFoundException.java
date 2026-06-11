package br.com.example.kazuhiro.controle_transaction.exceptions;

public class UserFoundException extends RuntimeException {
  public UserFoundException() {
    super("Usuário existente.");
  }
}
