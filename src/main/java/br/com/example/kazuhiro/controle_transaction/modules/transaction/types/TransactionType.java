package br.com.example.kazuhiro.controle_transaction.modules.transaction.types;

import java.util.Arrays;

public enum TransactionType {
  CREDIT('c'),
  DEBIT('d');

  private final char code;

  TransactionType(char code) {
    this.code = code;
  }

  public char getCode() {
    return code;
  }

  public static TransactionType fromCode(char code) {
    return Arrays.stream(values())
        .filter(type -> type.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown TransactionType code: " + code));
  }
}
