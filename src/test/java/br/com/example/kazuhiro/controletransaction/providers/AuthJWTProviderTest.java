package br.com.example.kazuhiro.controletransaction.providers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

class AuthJWTProviderTest {

  private AuthJWTProvider authJWTProvider;
  private final String secretKeyMock = "minha_chave_secreta_de_teste_123";

  @BeforeEach
  void setUp() {
    authJWTProvider = new AuthJWTProvider();
    ReflectionTestUtils.setField(authJWTProvider, "secretKey", secretKeyMock);
  }

  @Test
  @DisplayName("Deve validar o token com sucesso e retornar o JWT decodificado.")
  void shouldValidateTokenWithSuccess() {
    Algorithm algorithm = Algorithm.HMAC256(secretKeyMock);

    Clock fixedClock = Clock.fixed(Instant.parse("2060-06-13T21:00:00Z"), ZoneId.of("UTC"));
    Instant now = Instant.now(fixedClock);
    Instant expiresAt = now.plusSeconds(1800);

    String token = JWT.create()
        .withSubject("user-id-123")
        .withExpiresAt(expiresAt)
        .sign(algorithm);

    DecodedJWT result = authJWTProvider.validateToken("Bearer " + token);

    assertThat(result).isNotNull();
    assertThat(result.getSubject()).isEqualTo("user-id-123");
  }

  @Test
  @DisplayName("Deve retornar nulo quando o token estiver expirado ou for inválido.")
  void shouldReturnNullWhenTokenIsInvalid() {
    DecodedJWT result = authJWTProvider.validateToken("Bearer token_invalido_qualquer");

    assertThat(result).isNull();
  }
}
