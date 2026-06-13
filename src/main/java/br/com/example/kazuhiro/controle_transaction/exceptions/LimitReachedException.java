package br.com.example.kazuhiro.controle_transaction.exceptions;

public class LimitReachedException extends RuntimeException {
  public LimitReachedException() {
    super("Operação ilegal. Valor negativado supera o limite do usuário.");
  }
}
