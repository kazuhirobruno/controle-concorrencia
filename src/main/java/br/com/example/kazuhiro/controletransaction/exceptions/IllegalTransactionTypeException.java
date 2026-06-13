package br.com.example.kazuhiro.controletransaction.exceptions;

public class IllegalTransactionTypeException extends RuntimeException {
  public IllegalTransactionTypeException(String code) {
    super("Tipo de transação desconhecido: " + code);
  }
}
