package com.yourname.digishop.repository;

import com.yourname.digishop.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardCode(String cardCode);
    List<Card> findTop24ByNameContainingIgnoreCaseOrCardCodeContainingIgnoreCase(String name, String code);
}