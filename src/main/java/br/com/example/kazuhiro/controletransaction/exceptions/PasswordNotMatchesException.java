package br.com.example.kazuhiro.controletransaction.exceptions;

public class PasswordNotMatchesException extends RuntimeException {
  public PasswordNotMatchesException() {
    super("Senha e confirmação são diferentes.");
  }
}
