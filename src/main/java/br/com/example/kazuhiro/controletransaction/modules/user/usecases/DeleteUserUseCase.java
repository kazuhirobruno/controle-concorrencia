package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.ResourceNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
  private final UserRepository userRepository;

  @Transactional(readOnly = false)
  public void execute(UUID userId) {
    if (!this.userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("Recurso indisponível ou inexistente.");
    }

    this.userRepository.deleteById(userId);
  }
}
