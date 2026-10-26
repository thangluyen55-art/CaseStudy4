package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "card_sets")
@Getter @Setter @NoArgsConstructor
public class CardSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;            // BT1, EX1, ST1...

    @Column(nullable = false)
    private String name;

    private LocalDate releaseDate;
}