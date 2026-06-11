package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controle_transaction.exceptions.UserFoundException;
import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@Service
public class CreateUserUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserEntity execute(UserEntity userEntity) {
    this.userRepository.findByUsername(userEntity.getUsername())
        .ifPresent(user -> {
          throw new UserFoundException();
        });

    var password = passwordEncoder.encode(userEntity.getPassword());
    userEntity.setPassword(password);

    return this.userRepository.save(userEntity);

  }
}
