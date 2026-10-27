package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decks")
@Getter @Setter @NoArgsConstructor
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DeckCard> cards = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Số lá trong bộ bài chính (không tính Digi-Egg)
    public int getMainCount() {
        return cards.stream()
                .filter(dc -> dc.getCard().getCardType() != CardType.DIGI_EGG)
                .mapToInt(DeckCard::getQuantity).sum();
    }

    // Số Digi-Egg
    public int getEggCount() {
        return cards.stream()
                .filter(dc -> dc.getCard().getCardType() == CardType.DIGI_EGG)
                .mapToInt(DeckCard::getQuantity).sum();
    }

    public boolean isValid() {
        return getMainCount() == 50 && getEggCount() <= 5;
    }
}