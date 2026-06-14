package br.com.example.kazuhiro.controletransaction.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.example.kazuhiro.controletransaction.providers.AuthJWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Filtro de Segurança SecurityClienteFilter")
class SecurityClienteFilterTest {

  @Mock
  private AuthJWTProvider authJWTProvider;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private SecurityClienteFilter securityClienteFilter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @ParameterizedTest
  @CsvSource({
      "GET, /clientes",
      "PATCH, /clientes/outra-rota-qualquer",
      "POST, /clientes/notificacoes",
      "GET, /clientes/123/transacoes"
  })
  @DisplayName("Deve pular a validação e continuar a cadeia quando a rota não for monitorada pelo filtro")
  void shouldPassFilterWhenRouteIsNotMonitored(String httpMethod, String requestUri)
      throws ServletException, IOException {
    when(request.getMethod()).thenReturn(httpMethod);
    when(request.getRequestURI()).thenReturn(requestUri);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authJWTProvider, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("Deve retornar 401 Unauthorized quando o cabeçalho Authorization for nulo em rota protegida")
  void shouldReturnUnauthorizedWhenHeaderIsNull() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/clientes/123/transacoes");
    when(request.getHeader("Authorization")).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar 401 Unauthorized quando o cabeçalho não começar com Bearer")
  void shouldReturnUnauthorizedWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/clientes/123/transacoes");
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpzZW5oYQ==");

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar 401 Unauthorized quando o token for inválido")
  void shouldReturnUnauthorizedWhenTokenIsInvalid() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("PATCH");
    when(request.getRequestURI()).thenReturn("/clientes/password");
    when(request.getHeader("Authorization")).thenReturn("Bearer token_invalido");
    when(authJWTProvider.validateToken("token_invalido")).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve autenticar o usuário com sucesso no contexto do Spring Security na rota de Transações (POST)")
  void shouldAuthenticateUserWithSuccessOnTransactionRoute() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/clientes/123/transacoes");
    when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");

    DecodedJWT decodedJWTMock = mock(DecodedJWT.class);
    Claim rolesClaimMock = mock(Claim.class);

    when(decodedJWTMock.getSubject()).thenReturn("client-id-uuid");
    when(decodedJWTMock.getClaim("roles")).thenReturn(rolesClaimMock);
    when(rolesClaimMock.asList(Object.class)).thenReturn(List.of("CLIENT"));
    when(authJWTProvider.validateToken("token_valido")).thenReturn(decodedJWTMock);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(request).setAttribute("cliente_id", "client-id-uuid");
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("client-id-uuid");
  }

  @Test
  @DisplayName("Deve autenticar o usuário com sucesso no contexto do Spring Security na nova rota de Extrato (GET)")
  void shouldAuthenticateUserWithSuccessOnExtratoRoute() throws ServletException, IOException {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/clientes/a96f9ce4-7834-4f45-8f2b-c5a76dc7e1bc/extrato");
    when(request.getHeader("Authorization")).thenReturn("Bearer token_valido_extrato");

    DecodedJWT decodedJWTMock = mock(DecodedJWT.class);
    Claim rolesClaimMock = mock(Claim.class);

    when(decodedJWTMock.getSubject()).thenReturn("a96f9ce4-7834-4f45-8f2b-c5a76dc7e1bc");
    when(decodedJWTMock.getClaim("roles")).thenReturn(rolesClaimMock);
    when(rolesClaimMock.asList(Object.class)).thenReturn(List.of("CLIENT"));
    when(authJWTProvider.validateToken("token_valido_extrato")).thenReturn(decodedJWTMock);

    securityClienteFilter.doFilterInternal(request, response, filterChain);

    verify(request).setAttribute("cliente_id", "a96f9ce4-7834-4f45-8f2b-c5a76dc7e1bc");
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }
}
