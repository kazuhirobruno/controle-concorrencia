package br.com.example.kazuhiro.controle_transaction.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.example.kazuhiro.controle_transaction.providers.AuthJWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityClienteFilter extends OncePerRequestFilter {
  @Autowired
  private AuthJWTProvider authJWTProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (request.getRequestURI().contains("/clientes/password")
        || request.getRequestURI().contains("/clientes/delete")) {
      if (header == null || !header.startsWith("Bearer ")) {
        returnJsonError(response, "Token de autenticação ausente ou malformatado.",
            HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      String tokenPuro = header.replace("Bearer ", "");
      var tokenDecoded = this.authJWTProvider.validateToken(tokenPuro);

      if (tokenDecoded == null) {
        returnJsonError(response, "Token JWT inválido, expirado ou assinatura incorreta.",
            HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      request.setAttribute("cliente_id", tokenDecoded.getSubject());

      var roles = tokenDecoded.getClaim("roles").asList(Object.class);
      var grants = roles.stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
          .toList();

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          tokenDecoded.getSubject(),
          null,
          grants);

      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }

  private void returnJsonError(HttpServletResponse response, String message, int status) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");

    Map<String, Object> errorBody = Map.of(
        "message", message,
        "status", status,
        "timestamp", LocalDateTime.now().toString());

    response.getWriter().write(objectMapper.writeValueAsString(errorBody));
  }
}
