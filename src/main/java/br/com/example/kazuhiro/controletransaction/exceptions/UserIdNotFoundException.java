package br.com.example.kazuhiro.controletransaction.exceptions;

public class UserIdNotFoundException extends RuntimeException {
  public UserIdNotFoundException() {
    super("Recurso indisponível ou inexistente.");
  }
}
