package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
  private final UserRepository userRepository;

  @Transactional(readOnly = false)
  public void execute(UUID userId) {
    var user = this.userRepository.findById(userId).orElseThrow(() -> {
      throw new UserIdNotFoundException();
    });

    user.setActive(false);
    user.setUsername("deleted_" + System.currentTimeMillis());

    this.userRepository.save(user);
  }
}
