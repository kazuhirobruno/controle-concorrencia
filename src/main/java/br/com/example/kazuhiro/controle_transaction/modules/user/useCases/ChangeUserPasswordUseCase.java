package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.ChangeUserPasswordDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@Service
public class ChangeUserPasswordUseCase {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public void execute(ChangeUserPasswordDTO changeUserPasswordDTO, UUID client_id) {
    if (!changeUserPasswordDTO.getNewPassword().equals(changeUserPasswordDTO.getConfirmNewPassword())) {
      throw new IllegalArgumentException("A nova senha e a confirmação de senha não coincidem.");
    }

    var user = this.userRepository.findById(client_id).orElseThrow(() -> {
      throw new UsernameNotFoundException("Usuário não encontrado.");
    });

    String encryptedPassword = this.passwordEncoder.encode(changeUserPasswordDTO.getNewPassword());
    user.setPassword(encryptedPassword);

    this.userRepository.save(user);
  }
}
