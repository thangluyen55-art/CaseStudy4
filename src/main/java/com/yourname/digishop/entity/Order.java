package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")             // "order" là từ khóa SQL
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String receiverName;
    private String phone;
    private String shippingAddress;
    private String paymentMethod;                 // COD, BANK_TRANSFER

    @Column(precision = 12, scale = 0)
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(precision = 12, scale = 0)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    @Column(precision = 12, scale = 0)
    private BigDecimal discount = BigDecimal.ZERO;
    @Column(precision = 12, scale = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}