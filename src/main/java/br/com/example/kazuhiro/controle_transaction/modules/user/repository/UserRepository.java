package br.com.example.kazuhiro.controle_transaction.modules.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import br.com.example.kazuhiro.controle_transaction.modules.user.entitites.UserEntity;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<UserEntity> findById(UUID id);
}
