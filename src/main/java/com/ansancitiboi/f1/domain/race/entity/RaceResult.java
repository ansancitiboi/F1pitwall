package com.ansancitiboi.f1.domain.race.entity;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "race_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"race_id", "driver_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private Integer position;

    private BigDecimal points;

    private Boolean fastestLap;

    // FINISHED, DNF, DNS 등
    private String resultStatus;

    @Builder
    public RaceResult(Race race, Driver driver, Integer position,
                      BigDecimal points, Boolean fastestLap, String resultStatus) {
        this.race = race;
        this.driver = driver;
        this.position = position;
        this.points = points;
        this.fastestLap = fastestLap;
        this.resultStatus = resultStatus;
    }
}
