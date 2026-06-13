package br.com.example.kazuhiro.controle_transaction.modules.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controle_transaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controle_transaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@Service
public class UserBalanceService implements UserBalanceServiceInterface {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserEntity applyTransaction(UUID userId, String tipo, int valor) {
    var user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException());

    int multiplier = tipo.equals("c") ? 1 : -1;
    int saldo = user.getSaldo() + (valor * multiplier);
    if (saldo < (user.getLimite() * -1)) {
      throw new LimitReachedException();
    }

    user.setSaldo(saldo);
    return userRepository.save(user);
  }
}