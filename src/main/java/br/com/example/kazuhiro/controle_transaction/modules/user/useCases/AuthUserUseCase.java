package br.com.example.kazuhiro.controle_transaction.modules.user.useCases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.security.sasl.AuthenticationException;

import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserRequestDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.dtos.AuthUserResponseDTO;
import br.com.example.kazuhiro.controle_transaction.modules.user.repository.UserRepository;

@Service
public class AuthUserUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Value("${security.token.secret.client}")
  private String secretKey;

  public AuthUserResponseDTO execute(AuthUserRequestDTO authUserRequestDTO) throws AuthenticationException {
    var user = this.userRepository.findByUsername(authUserRequestDTO.getUsername()).orElseThrow(
        () -> {
          throw new UsernameNotFoundException("Username ou Password incorreto.");
        });

    var passwordMatches = this.passwordEncoder.matches(authUserRequestDTO.getPassword(), user.getPassword());

    if (!passwordMatches) {
      throw new AuthenticationException("Username ou Password incorreto.");
    }

    Algorithm algorithm = Algorithm.HMAC256(secretKey);
    var expires_in = Instant.now().plus(Duration.ofMinutes(30));
    var roles = Arrays.asList("CLIENT");
    var token = JWT.create()
        .withIssuer("transaction-control")
        .withSubject(user.getId().toString())
        .withClaim("roles", roles)
        .withExpiresAt(expires_in)
        .sign(algorithm);

    var authUserResponseDTO = AuthUserResponseDTO
        .builder()
        .expires_in(expires_in.toEpochMilli())
        .access_token(token)
        .roles(roles)
        .build();

    return authUserResponseDTO;
  }
}
