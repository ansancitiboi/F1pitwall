package com.ansancitiboi.f1.domain.driver.repository;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByDriverNumber(Integer driverNumber);

    boolean existsByDriverNumber(Integer driverNumber);
}
