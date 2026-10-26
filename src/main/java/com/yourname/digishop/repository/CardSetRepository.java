package com.yourname.digishop.repository;

import com.yourname.digishop.entity.CardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CardSetRepository extends JpaRepository<CardSet, Long> {
    Optional<CardSet> findByCode(String code);
}