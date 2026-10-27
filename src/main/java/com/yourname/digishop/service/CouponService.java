package com.yourname.digishop.service;

import com.yourname.digishop.entity.Coupon;
import com.yourname.digishop.entity.DiscountType;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository coupons;

    /** Tính tiền giảm để xem trước, không ghi gì vào database. */
    @Transactional(readOnly = true)
    public BigDecimal preview(String code, BigDecimal subtotal) {
        Coupon c = coupons.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));
        return calculate(c, subtotal);
    }

    /** Dùng mã khi đặt hàng: khóa dòng, kiểm tra lại, tăng lượt dùng. */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal redeem(String code, BigDecimal subtotal) {
        Coupon c = coupons.findByCodeForUpdate(code.trim())
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));
        BigDecimal discount = calculate(c, subtotal);
        c.setUsedCount(c.getUsedCount() + 1);
        return discount;
    }

    /** Hoàn lại một lượt dùng khi đơn bị hủy. */
    @Transactional
    public void release(String code) {
        if (code == null || code.isBlank()) return;
        coupons.findByCodeForUpdate(code.trim()).ifPresent(c -> {
            if (c.getUsedCount() > 0) c.setUsedCount(c.getUsedCount() - 1);
        });
    }

    private BigDecimal calculate(Coupon c, BigDecimal subtotal) {
        if (!c.isActive())
            throw new BusinessException("Mã giảm giá đã bị tắt");
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(LocalDate.now()))
            throw new BusinessException("Mã giảm giá đã hết hạn");
        if (c.getUsageLimit() != null && c.getUsedCount() >= c.getUsageLimit())
            throw new BusinessException("Mã giảm giá đã hết lượt sử dụng");
        if (c.getMinOrderValue() != null && subtotal.compareTo(c.getMinOrderValue()) < 0)
            throw new BusinessException("Đơn tối thiểu " + c.getMinOrderValue().toBigInteger()
                    + " đ mới dùng được mã này");

        BigDecimal discount = c.getDiscountType() == DiscountType.PERCENT
                ? subtotal.multiply(c.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : c.getDiscountValue();
        return discount.min(subtotal);                 // không giảm quá tổng tiền hàng
    }

    // ===== Quản trị =====
    @Transactional(readOnly = true)
    public List<Coupon> findAll() {
        return coupons.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(String code, DiscountType type, BigDecimal value, BigDecimal min,
                       Integer limit, LocalDate expiresAt) {
        String c = code == null ? "" : code.trim().toUpperCase();
        if (!c.matches("[A-Z0-9_-]{3,40}"))
            throw new BusinessException("Mã chỉ gồm chữ, số, dấu - và _ (3 đến 40 ký tự)");
        if (coupons.existsByCodeIgnoreCase(c))
            throw new BusinessException("Mã " + c + " đã tồn tại");
        if (value == null || value.signum() <= 0)
            throw new BusinessException("Giá trị giảm phải lớn hơn 0");
        if (type == DiscountType.PERCENT && value.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new BusinessException("Giảm theo phần trăm tối đa 100");
        if (limit != null && limit < 1)
            throw new BusinessException("Giới hạn lượt dùng phải từ 1 trở lên");

        Coupon n = new Coupon();
        n.setCode(c);
        n.setDiscountType(type);
        n.setDiscountValue(value);
        n.setMinOrderValue(min);
        n.setUsageLimit(limit);
        n.setExpiresAt(expiresAt);
        coupons.save(n);
    }

    @Transactional
    public void toggle(Long id) {
        Coupon c = coupons.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy mã giảm giá"));
        c.setActive(!c.isActive());
    }
}