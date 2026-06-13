package br.com.example.kazuhiro.controletransaction.modules.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBalanceService implements UserBalanceServiceInterface {

  private final UserRepository userRepository;

  @Override
  public UserEntity applyTransaction(UUID userId, String tipo, int valor) {
    var user = userRepository.findById(userId).orElseThrow(UserIdNotFoundException::new);

    int multiplier = tipo.equals("c") ? 1 : -1;
    int saldo = user.getSaldo() + (valor * multiplier);
    if (saldo < (user.getLimite() * -1)) {
      throw new LimitReachedException();
    }

    user.setSaldo(saldo);
    return userRepository.save(user);
  }
}