package com.DigitalMoneyHouse.accountsservice.repository;

import com.DigitalMoneyHouse.accountsservice.entities.Transaction;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop5ByAccountIdOrderByDateDesc(Long accountId);
}
