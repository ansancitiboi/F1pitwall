package com.ansancitiboi.f1.domain.driver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer driverNumber;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String teamName;

    private String nationality;

    private String headshotUrl;

    @Builder
    public Driver(Integer driverNumber, String code, String firstName, String lastName,
                  String teamName, String nationality, String headshotUrl) {
        this.driverNumber = driverNumber;
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.teamName = teamName;
        this.nationality = nationality;
        this.headshotUrl = headshotUrl;
    }

    public void update(String code, String firstName, String lastName,
                       String teamName, String nationality, String headshotUrl) {
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.teamName = teamName;
        this.nationality = nationality;
        this.headshotUrl = headshotUrl;
    }
}
