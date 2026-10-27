package com.yourname.digishop.config;

import com.yourname.digishop.entity.Coupon;
import com.yourname.digishop.entity.DiscountType;
import com.yourname.digishop.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CouponSeeder implements CommandLineRunner {
    private final CouponRepository coupons;

    @Override
    public void run(String... args) {
        add("DIGI10", DiscountType.PERCENT, "10", "100000", 100);     // giảm 10%, đơn từ 100.000 đ
        add("GIAM50K", DiscountType.AMOUNT, "50000", "300000", 50);   // giảm 50.000 đ, đơn từ 300.000 đ
    }

    private void add(String code, DiscountType type, String value, String min, int limit) {
        if (coupons.existsByCodeIgnoreCase(code)) return;
        Coupon c = new Coupon();
        c.setCode(code);
        c.setDiscountType(type);
        c.setDiscountValue(new BigDecimal(value));
        c.setMinOrderValue(new BigDecimal(min));
        c.setUsageLimit(limit);
        c.setExpiresAt(LocalDate.now().plusYears(1));
        coupons.save(c);
    }
}