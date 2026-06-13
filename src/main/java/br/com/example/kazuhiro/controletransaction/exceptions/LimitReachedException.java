package br.com.example.kazuhiro.controletransaction.exceptions;

public class LimitReachedException extends RuntimeException {
  public LimitReachedException() {
    super("Operação ilegal. Valor negativado supera o limite do usuário.");
  }
}
