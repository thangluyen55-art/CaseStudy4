package com.yourname.digishop.repository;

import com.yourname.digishop.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserEmailOrderByIdDesc(String email);
    Optional<WishlistItem> findByUserEmailAndProductId(String email, Long productId);
    boolean existsByUserEmailAndProductId(String email, Long productId);
}