package br.com.example.kazuhiro.controletransaction.modules.user.usecases;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.example.kazuhiro.controletransaction.modules.user.dtos.AuthUserRequestDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.dtos.AuthUserResponseDTO;
import br.com.example.kazuhiro.controletransaction.modules.user.repository.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.security.sasl.AuthenticationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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
    var expiresIn = Instant.now(Clock.systemUTC()).plus(Duration.ofMinutes(30));
    var roles = Arrays.asList("CLIENT");
    var token = JWT.create()
        .withIssuer("transaction-control")
        .withSubject(user.getId().toString())
        .withClaim("roles", roles)
        .withExpiresAt(expiresIn)
        .sign(algorithm);

    return AuthUserResponseDTO
        .builder()
        .expiresIn(expiresIn.toEpochMilli())
        .accessToken(token)
        .roles(roles)
        .build();
  }
}
