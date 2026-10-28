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
    private final CouponService couponService;

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

        // Khóa sản phẩm theo thứ tự id tăng dần để tránh deadlock
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

            p.setStock(p.getStock() - ci.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(p.getPrice());
            order.getItems().add(oi);

            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // Mã giảm giá (tùy chọn)
        BigDecimal discount = BigDecimal.ZERO;
        String code = form.getCouponCode();
        if (code != null && !code.isBlank()) {
            discount = couponService.redeem(code, subtotal);
            order.setCouponCode(code.trim().toUpperCase());
        }

        order.setSubtotal(subtotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscount(discount);
        order.setTotalAmount(subtotal.add(SHIPPING_FEE).subtract(discount));

        orders.save(order);
        cart.getItems().clear();
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
        Order order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new BusinessException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException(
                    "Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Chỉ hủy được đơn đang chờ xác nhận");
        }

        // Khóa sản phẩm và hoàn kho.
        List<OrderItem> items = new ArrayList<>(order.getItems());
        items.sort(Comparator.comparing(
                item -> item.getProduct().getId()));

        for (OrderItem item : items) {
            Product product = products
                    .findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Không tìm thấy sản phẩm trong đơn"));

            product.setStock(
                    product.getStock() + item.getQuantity());
        }

        // Hoàn lượt coupon trong cùng transaction.
        couponService.release(order.getCouponCode());

        order.setStatus(OrderStatus.CANCELLED);
    }

}