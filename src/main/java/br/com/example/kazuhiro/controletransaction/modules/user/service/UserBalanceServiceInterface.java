package br.com.example.kazuhiro.controletransaction.modules.user.service;

import java.util.UUID;

import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;

public interface UserBalanceServiceInterface {
  UserEntity applyTransaction(UUID userId, String tipo, Long valor);
}