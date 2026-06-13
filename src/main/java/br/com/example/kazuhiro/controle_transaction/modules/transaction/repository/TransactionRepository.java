package br.com.example.kazuhiro.controle_transaction.modules.transaction.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.example.kazuhiro.controle_transaction.modules.transaction.entities.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

}
