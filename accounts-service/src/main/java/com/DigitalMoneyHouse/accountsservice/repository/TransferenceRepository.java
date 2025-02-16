package com.DigitalMoneyHouse.accountsservice.repository;

import com.DigitalMoneyHouse.accountsservice.entities.Transference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferenceRepository extends JpaRepository<Transference, Long> {
    List<Transference> findTop5ByAccountIdOrderByDateDesc(Long accountId);
}

