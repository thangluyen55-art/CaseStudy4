package com.yourname.digishop.service;

import com.yourname.digishop.entity.*;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository carts;
    private final UserRepository users;
    private final ProductRepository products;

    @Transactional
    public Cart getOrCreateCart(String email) {
        return carts.findByUserEmail(email).orElseGet(() -> {
            User u = users.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));
            Cart c = new Cart();
            c.setUser(u);
            return carts.save(c);
        });
    }

    @Transactional
    public void addItem(String email, Long productId, int qty) {
        if (qty < 1) throw new BusinessException("Số lượng phải từ 1 trở lên");

        Product p = products.findById(productId).filter(Product::isActive)
                .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại hoặc đã ngừng bán"));
        if (p.getStock() <= 0)
            throw new BusinessException("\"" + p.getDisplayName() + "\" đã hết hàng");

        Cart cart = getOrCreateCart(email);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst().orElse(null);

        int newQty = (item == null ? 0 : item.getQuantity()) + qty;
        if (newQty > p.getStock())
            throw new BusinessException("Chỉ còn " + p.getStock() + " sản phẩm \""
                    + p.getDisplayName() + "\"");

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(p);
            cart.getItems().add(item);
        }
        item.setQuantity(newQty);
    }

    @Transactional
    public void updateQuantity(String email, Long itemId, int qty) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm trong giỏ"));

        if (qty <= 0) {
            cart.getItems().remove(item);               // orphanRemoval tự xóa
            return;
        }
        if (qty > item.getProduct().getStock())
            throw new BusinessException("Chỉ còn " + item.getProduct().getStock()
                    + " sản phẩm \"" + item.getProduct().getDisplayName() + "\"");
        item.setQuantity(qty);
    }

    @Transactional
    public void removeItem(String email, Long itemId) {
        updateQuantity(email, itemId, 0);
    }
}