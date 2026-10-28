package com.yourname.digishop.repository;

import com.yourname.digishop.entity.Order;
import com.yourname.digishop.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);


    // Tổng tiền các đơn theo trạng thái (null nếu chưa có đơn nào)
    @Query("select sum(o.totalAmount) from Order o where o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);

    // Doanh thu theo tháng: [năm, tháng, tổng tiền], mới nhất trước
    @Query("select extract(year from o.createdAt), extract(month from o.createdAt), sum(o.totalAmount) " +
            "from Order o where o.status = :status " +
            "group by extract(year from o.createdAt), extract(month from o.createdAt) " +
            "order by extract(year from o.createdAt) desc, extract(month from o.createdAt) desc")
    List<Object[]> revenueByMonth(@Param("status") OrderStatus status, Pageable pageable);

    // Sản phẩm bán chạy: [productId, tổng số lượng]
    @Query("select oi.product.id, sum(oi.quantity) from OrderItem oi " +
            "where oi.order.status <> :excluded " +
            "group by oi.product.id order by sum(oi.quantity) desc")
    List<Object[]> topSelling(@Param("excluded") OrderStatus excluded, Pageable pageable);
}