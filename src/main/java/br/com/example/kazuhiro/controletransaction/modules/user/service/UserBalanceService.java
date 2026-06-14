package br.com.example.kazuhiro.controletransaction.modules.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.LimitReachedException;
import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBalanceService implements UserBalanceServiceInterface {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public UserEntity applyTransaction(UUID userId, String tipo, Long valor) {
    var user = userRepository.findWithLockById(userId).orElseThrow(UserIdNotFoundException::new);

    if (!user.isActive()) {
      throw new ResourceNotFoundException("Recurso não encontrado.");
    }

    int multiplier = tipo.equals("c") ? 1 : -1;
    Long saldo = user.getSaldo() + (valor * multiplier);
    if (saldo < (user.getLimite() * -1)) {
      throw new LimitReachedException();
    }

    user.setSaldo(saldo);
    return userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserEntity findActiveUserById(UUID userId) {
    var user = userRepository.findPureById(userId).orElseThrow(UserIdNotFoundException::new);

    if (!user.isActive()) {
      throw new ResourceNotFoundException("Recurso não encontrado.");
    }

    return user;
  }
}