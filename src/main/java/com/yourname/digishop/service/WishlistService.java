package com.yourname.digishop.service;

import com.yourname.digishop.entity.Product;
import com.yourname.digishop.entity.User;
import com.yourname.digishop.entity.WishlistItem;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.ProductRepository;
import com.yourname.digishop.repository.UserRepository;
import com.yourname.digishop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlist;
    private final UserRepository users;
    private final ProductRepository products;

    @Transactional(readOnly = true)
    public List<WishlistItem> list(String email) {
        return wishlist.findByUserEmailOrderByIdDesc(email);
    }

    @Transactional(readOnly = true)
    public boolean contains(String email, Long productId) {
        return wishlist.existsByUserEmailAndProductId(email, productId);
    }

    /** Bật/tắt yêu thích. Trả về true nếu vừa thêm, false nếu vừa bỏ. */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(String email, Long productId) {
        var existing = wishlist.findByUserEmailAndProductId(email, productId);
        if (existing.isPresent()) {
            wishlist.delete(existing.get());
            return false;
        }
        User u = users.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));
        Product p = products.findById(productId).filter(Product::isActive)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
        WishlistItem w = new WishlistItem();
        w.setUser(u);
        w.setProduct(p);
        wishlist.save(w);
        return true;
    }
}