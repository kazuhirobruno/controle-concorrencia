package br.com.example.kazuhiro.controle_transaction.modules.user.service;

import java.util.UUID;

import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;

public interface UserBalanceServiceInterface {
  UserEntity applyTransaction(UUID userId, String tipo, int valor);
}