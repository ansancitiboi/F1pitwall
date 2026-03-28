package com.ansancitiboi.f1.domain.race.entity;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "driver_standings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"season", "driver_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriverStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private Integer position;

    private BigDecimal points;

    private Integer wins;

    @Builder
    public DriverStanding(Integer season, Driver driver, Integer position,
                          BigDecimal points, Integer wins) {
        this.season = season;
        this.driver = driver;
        this.position = position;
        this.points = points;
        this.wins = wins;
    }

    public void update(Integer position, BigDecimal points, Integer wins) {
        this.position = position;
        this.points = points;
        this.wins = wins;
    }
}
