package com.yourname.digishop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType = ProductType.SINGLE;

    @Column(unique = true)
    private String sku;                    // BT1-BOX, BT1-PACK, ST1-DECK...

    private String name;                   // tên hiển thị cho hàng sealed
    private String imageUrl;

    // ----- Chỉ dùng cho SINGLE -----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    // ----- Dùng cho BOOSTER_BOX, BOOSTER_PACK, STARTER_DECK -----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id")
    private CardSet cardSet;

    private Integer packsPerBox;           // 24, chỉ BOOSTER_BOX
    private Integer cardsPerPack;          // 12, BOOSTER_BOX và BOOSTER_PACK
    private Integer cardsPerDeck;          // 54, chỉ STARTER_DECK

    // ----- Chung -----
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    private int stock;
    private boolean active = true;

    @Version
    private Long version;                  // khóa lạc quan, dùng khi trừ kho

    // ----- Tiện ích -----
    public boolean isSingle() {
        return productType == ProductType.SINGLE;
    }

    public boolean isSealed() {
        return productType != ProductType.SINGLE;
    }

    /** Tên hiển thị dùng chung trong giỏ hàng, đơn hàng, danh sách. */
    public String getDisplayName() {
        if (isSingle() && card != null) {
            return card.getName() + " (" + card.getCardCode() + ")";
        }
        return name;
    }

    /** Đảm bảo dữ liệu nhất quán theo từng loại hàng. */
    @PrePersist
    @PreUpdate
    private void validateByType() {
        if (isSingle()) {
            if (card == null)
                throw new IllegalStateException("Thẻ lẻ phải gắn với dữ liệu thẻ");
        } else {
            if (name == null || name.isBlank() || cardSet == null || sku == null)
                throw new IllegalStateException("Hàng sealed phải có sku, tên và set");
            if (card != null)
                throw new IllegalStateException("Hàng sealed không gắn với thẻ lẻ");
        }
    }

    public String getImage() {
        if (isSingle() && card != null) {
            return "/card-images/" + card.getCardCode() + ".webp";
        }
        return imageUrl;     // hàng sealed: "/card-images/BT26-BOX.webp" nếu seeder đã đặt, hoặc null
    }
}