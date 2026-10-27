package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal discountValue;          // PERCENT: 10 nghĩa là 10%, AMOUNT: số tiền

    @Column(precision = 12, scale = 0)
    private BigDecimal minOrderValue;          // null = không yêu cầu

    private Integer usageLimit;                // null = không giới hạn
    private int usedCount = 0;
    private LocalDate expiresAt;               // null = không hết hạn
    private boolean active = true;
}