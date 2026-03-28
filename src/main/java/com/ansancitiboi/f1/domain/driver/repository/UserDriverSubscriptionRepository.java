package com.ansancitiboi.f1.domain.driver.repository;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import com.ansancitiboi.f1.domain.driver.entity.UserDriverSubscription;
import com.ansancitiboi.f1.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDriverSubscriptionRepository extends JpaRepository<UserDriverSubscription, Long> {

    boolean existsByUserAndDriver(User user, Driver driver);

    Optional<UserDriverSubscription> findByUserAndDriver(User user, Driver driver);

    @Query("SELECT s.driver FROM UserDriverSubscription s WHERE s.user.id = :userId")
    List<Driver> findDriversByUserId(Long userId);

    @Query("SELECT s.user.id FROM UserDriverSubscription s WHERE s.driver.id = :driverId")
    List<Long> findUserIdsByDriverId(Long driverId);
}
