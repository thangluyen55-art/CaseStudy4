package com.yourname.digishop.repository;

import com.yourname.digishop.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByUserEmailOrderByCreatedAtDesc(String email);
}