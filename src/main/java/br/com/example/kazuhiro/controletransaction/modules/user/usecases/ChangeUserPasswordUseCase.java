package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.example.kazuhiro.controletransaction.exceptions.UserIdNotFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeUserPasswordUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = false)
  public void execute(ChangeUserPasswordDTO changeUserPasswordDTO, UUID clientId) {
    if (!changeUserPasswordDTO.getNewPassword().equals(changeUserPasswordDTO.getConfirmNewPassword())) {
      throw new IllegalArgumentException("A nova senha e a confirmação de senha não coincidem.");
    }

    var user = this.userRepository.findById(clientId).orElseThrow(() -> {
      throw new UserIdNotFoundException();
    });

    String encryptedPassword = this.passwordEncoder.encode(changeUserPasswordDTO.getNewPassword());
    user.setPassword(encryptedPassword);

    this.userRepository.save(user);
  }
}
