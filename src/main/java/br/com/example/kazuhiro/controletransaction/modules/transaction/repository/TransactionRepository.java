package br.com.example.kazuhiro.controletransaction.modules.transaction.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.example.kazuhiro.controletransaction.modules.transaction.entities.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

}
