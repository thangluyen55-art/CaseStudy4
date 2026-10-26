package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cards")
@Getter @Setter @NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardCode;        // BT1-009

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "set_id")
    private CardSet cardSet;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private CardColor color;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    private Integer level;          // Tamer/Option có thể null
    private Integer playCost;
    private Integer dp;

    @Column(length = 1000)
    private String effect;

    private String imageUrl;
}