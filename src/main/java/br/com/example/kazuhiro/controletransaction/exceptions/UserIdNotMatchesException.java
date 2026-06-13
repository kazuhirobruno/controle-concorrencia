package br.com.example.kazuhiro.controletransaction.exceptions;

public class UserIdNotMatchesException extends RuntimeException {
  public UserIdNotMatchesException() {
    super("UserId de URL e Token não são iguais.");
  }
}
