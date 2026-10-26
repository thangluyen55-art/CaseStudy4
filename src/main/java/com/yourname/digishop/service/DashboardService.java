package com.yourname.digishop.service;

import com.yourname.digishop.entity.OrderStatus;
import com.yourname.digishop.entity.Product;
import com.yourname.digishop.repository.OrderRepository;
import com.yourname.digishop.repository.ProductRepository;
import com.yourname.digishop.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    @Getter @AllArgsConstructor
    public static class MonthRevenue { private String label; private BigDecimal amount; private int percent; }

    @Getter @AllArgsConstructor
    public static class TopProduct { private String name; private long quantity; }

    @Getter @AllArgsConstructor
    public static class StatusCount { private OrderStatus status; private long count; }

    @Getter @AllArgsConstructor
    public static class Dashboard {
        private BigDecimal revenue;
        private long totalOrders;
        private long pendingOrders;
        private long productCount;
        private long customerCount;
        private List<StatusCount> byStatus;
        private List<MonthRevenue> months;
        private List<TopProduct> top;
        private List<Product> lowStock;
    }

    @Transactional(readOnly = true)
    public Dashboard build() {
        BigDecimal revenue = orders.sumTotalByStatus(OrderStatus.COMPLETED);   // chỉ tính đơn hoàn tất
        if (revenue == null) revenue = BigDecimal.ZERO;

        List<StatusCount> byStatus = new ArrayList<>();
        for (OrderStatus s : OrderStatus.values())
            byStatus.add(new StatusCount(s, orders.countByStatus(s)));

        // Doanh thu 6 tháng gần nhất, xếp từ cũ đến mới
        List<Object[]> rows = new ArrayList<>(orders.revenueByMonth(OrderStatus.COMPLETED, PageRequest.of(0, 6)));
        Collections.reverse(rows);
        BigDecimal max = rows.stream().map(r -> (BigDecimal) r[2])
                .max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        List<MonthRevenue> months = new ArrayList<>();
        for (Object[] r : rows) {
            BigDecimal amount = (BigDecimal) r[2];
            int percent = max.signum() == 0 ? 0 : amount.multiply(BigDecimal.valueOf(100))
                    .divide(max, 0, java.math.RoundingMode.HALF_UP).intValue();
            months.add(new MonthRevenue(String.format("%02d/%d", ((Number) r[1]).intValue(),
                    ((Number) r[0]).intValue()), amount, percent));
        }

        // 5 sản phẩm bán chạy (bỏ qua đơn đã hủy)
        List<TopProduct> top = new ArrayList<>();
        for (Object[] r : orders.topSelling(OrderStatus.CANCELLED, PageRequest.of(0, 5))) {
            Long pid = ((Number) r[0]).longValue();
            String name = products.findById(pid).map(Product::getDisplayName).orElse("#" + pid);
            top.add(new TopProduct(name, ((Number) r[1]).longValue()));
        }

        return new Dashboard(revenue, orders.count(), orders.countByStatus(OrderStatus.PENDING),
                products.count(), users.countByRoleName("ROLE_CUSTOMER"), byStatus, months, top,
                products.findByActiveTrueAndStockLessThanEqualOrderByStockAsc(2, PageRequest.of(0, 8)));
    }
}