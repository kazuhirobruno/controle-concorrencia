package br.com.example.kazuhiro.controle_transaction.exceptions;

public class UserIdNotFoundException extends RuntimeException {
  public UserIdNotFoundException() {
    super("Usuário não encontrado.");
  }
}
