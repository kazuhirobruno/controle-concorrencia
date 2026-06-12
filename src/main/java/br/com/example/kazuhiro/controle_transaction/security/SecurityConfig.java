package br.com.example.kazuhiro.controle_transaction.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private static final String[] PERMIT_ALL_LIST = {
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/swagger-resource/**",
      "/actuator/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(PERMIT_ALL_LIST).permitAll()
              .requestMatchers("/clientes/").permitAll()
              .requestMatchers("/clientes/auth").permitAll();
          auth.anyRequest().authenticated();
        });

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

}
