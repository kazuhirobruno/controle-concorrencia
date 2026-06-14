package br.com.example.kazuhiro.controletransaction.modules.transaction.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

  @Query(value = "SELECT * FROM transaction WHERE client_id = :clientId ORDER BY realizada_em DESC LIMIT 10", nativeQuery = true)
  List<TransactionEntity> findTop10ByClienteIdOrderByRealizadaEmDesc(@Param("clientId") UUID clientId);
}
