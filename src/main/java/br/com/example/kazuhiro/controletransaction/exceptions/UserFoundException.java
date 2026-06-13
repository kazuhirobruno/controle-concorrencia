package br.com.example.kazuhiro.controletransaction.exceptions;

public class UserFoundException extends RuntimeException {
  public UserFoundException() {
    super("Usuário existente.");
  }
}
