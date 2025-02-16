package com.DigitalMoneyHouse.accountsservice.repository;


import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByAccountIdOrderByDateDesc(Long accountId);
    Optional<Activity> findByAccountIdAndId(Long accountId, Long id);

    // Método para filtrar actividades
    @Query("SELECT a FROM Activity a WHERE " +
            "(:minAmount IS NULL OR a.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR a.amount <= :maxAmount) AND " +
            "(:startDate IS NULL OR a.date >= :startDate) AND " +
            "(:endDate IS NULL OR a.date <= :endDate) AND " +
            "(:activityType IS NULL OR a.type = :activityType)")
    List<Activity> filterActivities(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("activityType") String activityType
    );

    List<Activity> findTop5ByAccountIdOrderByDateDesc(Long accountId);
}
