package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@Service
public class DeleteUserUseCase {
  @Autowired
  private UserRepository userRepository;

  public void execute(UUID user_id) {
    if (!this.userRepository.existsById(user_id)) {
      throw new UsernameNotFoundException("Usuário não encontrado.");
    }

    this.userRepository.deleteById(user_id);
  }
}
