package com.yourname.digishop.service;

import com.yourname.digishop.entity.Order;
import com.yourname.digishop.entity.OrderItem;
import com.yourname.digishop.entity.OrderStatus;
import com.yourname.digishop.entity.Product;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.OrderRepository;
import com.yourname.digishop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orders;
    private final ProductRepository products;

    // Máy trạng thái: trạng thái hiện tại -> các trạng thái được phép chuyển tới
    private static final Map<OrderStatus, List<OrderStatus>> NEXT = Map.of(
            OrderStatus.PENDING,   List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, List.of(OrderStatus.PACKING, OrderStatus.CANCELLED),
            OrderStatus.PACKING,   List.of(OrderStatus.SHIPPING),
            OrderStatus.SHIPPING,  List.of(OrderStatus.COMPLETED));

    public List<OrderStatus> nextStatuses(OrderStatus current) {
        return NEXT.getOrDefault(current, List.of());
    }

    @Transactional(readOnly = true)
    public Page<Order> search(OrderStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
        return status == null ? orders.findAll(pageable) : orders.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Order get(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng #" + id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, OrderStatus next) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng #" + id));

        if (!nextStatuses(order.getStatus()).contains(next))
            throw new BusinessException("Không thể chuyển đơn từ " + order.getStatus()
                    + " sang " + next);

        if (next == OrderStatus.CANCELLED) {                       // hủy đơn: hoàn kho
            for (OrderItem oi : order.getItems()) {
                Product p = products.findByIdForUpdate(oi.getProduct().getId()).orElseThrow();
                p.setStock(p.getStock() + oi.getQuantity());
            }
        }
        order.setStatus(next);
    }
}