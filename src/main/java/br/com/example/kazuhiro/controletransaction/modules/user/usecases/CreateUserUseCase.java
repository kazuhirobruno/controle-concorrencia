package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.example.kazuhiro.controletransaction.exceptions.PasswordNotMatchesException;
import br.com.example.kazuhiro.controletransaction.exceptions.UserFoundException;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.CreateUserDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserEntity execute(CreateUserDTO createUserDTO) {
    if (!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword())) {
      throw new PasswordNotMatchesException();
    }

    this.userRepository.findByUsername(createUserDTO.getUsername())
        .ifPresent(user -> {
          throw new UserFoundException();
        });

    var passwordEncoded = passwordEncoder.encode(createUserDTO.getPassword());
    var userEntity = UserEntity.builder()
        .username(createUserDTO.getUsername())
        .password(passwordEncoded)
        .limite(createUserDTO.getLimite())
        .saldo(createUserDTO.getSaldo())
        .active(true)
        .build();

    return this.userRepository.save(userEntity);

  }
}
