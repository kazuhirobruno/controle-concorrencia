package br.com.example.kazuhiro.controle_transaction.modules.user.entitites;

import java.util.UUID;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Entity(name = "client")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Length(max = 20)
  private String username;

  @NotBlank
  @Length(max = 20)
  private String password;

  @PositiveOrZero
  private int limite;

  private int saldo;

}
