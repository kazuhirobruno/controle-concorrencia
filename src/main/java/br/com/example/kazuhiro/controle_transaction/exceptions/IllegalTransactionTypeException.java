package br.com.example.kazuhiro.controle_transaction.exceptions;

public class IllegalTransactionTypeException extends RuntimeException {
  public IllegalTransactionTypeException(String code) {
    super("Tipo de transação desconhecido: " + code);
  }
}
