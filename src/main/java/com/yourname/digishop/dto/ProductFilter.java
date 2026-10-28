package com.yourname.digishop.dto;

import com.yourname.digishop.entity.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ProductFilter {
    private String keyword;
    private ProductType productType;
    private Long setId;
    private CardColor color;
    private CardType cardType;
    private Rarity rarity;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}