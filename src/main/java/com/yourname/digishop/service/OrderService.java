package com.yourname.digishop.service;

import com.yourname.digishop.dto.CheckoutForm;
import com.yourname.digishop.entity.*;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    public static final BigDecimal SHIPPING_FEE = new BigDecimal("30000");

    private final CartRepository carts;
    private final ProductRepository products;
    private final OrderRepository orders;

    @Transactional(rollbackFor = Exception.class)
    public Order placeOrder(String email, CheckoutForm form) {
        Cart cart = carts.findByUserEmail(email)
                .orElseThrow(() -> new BusinessException("Giỏ hàng trống"));
        if (cart.getItems().isEmpty()) throw new BusinessException("Giỏ hàng trống");

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setReceiverName(form.getReceiverName().trim());
        order.setPhone(form.getPhone().trim());
        order.setShippingAddress(form.getAddress().trim());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);

        // Khóa sản phẩm theo thứ tự id tăng dần để tránh deadlock khi nhiều người mua cùng lúc
        List<CartItem> sorted = new ArrayList<>(cart.getItems());
        sorted.sort(Comparator.comparing(ci -> ci.getProduct().getId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : sorted) {
            Product p = products.findByIdForUpdate(ci.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("Sản phẩm không còn tồn tại"));
            if (!p.isActive())
                throw new BusinessException("\"" + p.getDisplayName() + "\" đã ngừng bán");
            if (p.getStock() < ci.getQuantity())
                throw new BusinessException("Không đủ hàng: \"" + p.getDisplayName()
                        + "\" (còn " + p.getStock() + ")");

            p.setStock(p.getStock() - ci.getQuantity());         // trừ kho

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(p.getPrice());                       // lưu giá lúc mua
            order.getItems().add(oi);

            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setSubtotal(subtotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscount(BigDecimal.ZERO);                      // mã giảm giá làm ở Sprint 6
        order.setTotalAmount(subtotal.add(SHIPPING_FEE));

        orders.save(order);
        cart.getItems().clear();                                 // dọn giỏ
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersOfUser(String email) {
        return orders.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Order getOrderOfUser(Long orderId, String email) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng"));
        if (!order.getUser().getEmail().equals(email))
            throw new BusinessException("Bạn không có quyền xem đơn hàng này");
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String email) {
        Order order = getOrderOfUser(orderId, email);
        if (order.getStatus() != OrderStatus.PENDING)
            throw new BusinessException("Chỉ hủy được đơn đang chờ xác nhận");

        for (OrderItem oi : order.getItems()) {                  // hoàn kho
            Product p = products.findByIdForUpdate(oi.getProduct().getId()).orElseThrow();
            p.setStock(p.getStock() + oi.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }
}