package com.ansancitiboi.f1.domain.race.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "races")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    @Column(nullable = false)
    private Integer round;

    @Column(nullable = false)
    private String raceName;

    private String circuit;

    private String country;

    private LocalDate raceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RaceStatus status;

    // OpenF1 세션 키 (실시간 폴링 시 사용)
    private Integer sessionKey;

    @Builder
    public Race(Integer season, Integer round, String raceName, String circuit,
                String country, LocalDate raceDate, RaceStatus status, Integer sessionKey) {
        this.season = season;
        this.round = round;
        this.raceName = raceName;
        this.circuit = circuit;
        this.country = country;
        this.raceDate = raceDate;
        this.status = status;
        this.sessionKey = sessionKey;
    }

    public void updateStatus(RaceStatus status) {
        this.status = status;
    }

    public void updateSessionKey(Integer sessionKey) {
        this.sessionKey = sessionKey;
    }
}
