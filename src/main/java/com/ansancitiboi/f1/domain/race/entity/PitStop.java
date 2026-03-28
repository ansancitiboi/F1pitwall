package com.ansancitiboi.f1.domain.race.entity;

import com.ansancitiboi.f1.domain.driver.entity.Driver;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "pit_stops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PitStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private Integer lap;

    private Integer stop;

    private String duration;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PitStop(Race race, Driver driver, Integer lap, Integer stop, String duration) {
        this.race = race;
        this.driver = driver;
        this.lap = lap;
        this.stop = stop;
        this.duration = duration;
    }
}
