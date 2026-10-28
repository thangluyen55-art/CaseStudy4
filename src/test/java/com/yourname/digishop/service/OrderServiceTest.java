
package com.yourname.digishop.service;

import com.yourname.digishop.entity.Order;
import com.yourname.digishop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void orderRepositoryCanBeUsedInTransaction() {
        assertNotNull(orderRepository);
    }

    // TODO: Bổ sung test nghiệp vụ bằng dữ liệu test được chuẩn bị riêng:
    // 1. Đặt hàng thành công và kiểm tra tổng tiền.
    // 2. Đặt hàng khi thiếu tồn kho phải thất bại.
    // 3. Đặt hàng thất bại không được lưu đơn hoặc trừ kho.
    // 4. Hủy đơn chỉ hoàn kho một lần.
    // 5. Coupon hết lượt không được redeem.
}
