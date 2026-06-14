package br.com.example.kazuhiro.controletransaction.modules.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.example.kazuhiro.controletransaction.modules.user.entitites.UserEntity;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<UserEntity> findById(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<UserEntity> findWithLockById(UUID id);

  @Query(value = "SELECT * FROM client WHERE id = :id", nativeQuery = true)
  Optional<UserEntity> findPureById(@Param("id") UUID id);
}
