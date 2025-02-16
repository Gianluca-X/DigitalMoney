package com.DigitalMoneyHouse.accountsservice.repository;

import com.DigitalMoneyHouse.accountsservice.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByAccountId(Long accountId); // Obtener tarjetas por ID de cuenta
    Optional<Card> findByNumber(String number);
}
