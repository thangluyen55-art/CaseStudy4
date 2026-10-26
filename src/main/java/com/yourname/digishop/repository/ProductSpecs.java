package com.yourname.digishop.repository;

import com.yourname.digishop.dto.ProductFilter;
import com.yourname.digishop.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecs {

    public static Specification<Product> filter(ProductFilter f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            Join<Product, Card> card = root.join("card", JoinType.LEFT);

            ps.add(cb.isTrue(root.get("active")));

            if (f.getProductType() != null)
                ps.add(cb.equal(root.get("productType"), f.getProductType()));

            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                String k = "%" + f.getKeyword().trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(card.get("name")), k),
                        cb.like(cb.lower(card.get("cardCode")), k),
                        cb.like(cb.lower(root.get("name")), k)));
            }

            // Set: thẻ lẻ lọc qua card.cardSet, hàng sealed lọc qua product.cardSet
            if (f.getSetId() != null)
                ps.add(cb.or(
                        cb.equal(card.get("cardSet").get("id"), f.getSetId()),
                        cb.equal(root.get("cardSet").get("id"), f.getSetId())));

            // Các bộ lọc dưới đây chỉ có nghĩa với thẻ lẻ
            if (f.getColor() != null)
                ps.add(cb.equal(card.get("color"), f.getColor()));
            if (f.getCardType() != null)
                ps.add(cb.equal(card.get("cardType"), f.getCardType()));
            if (f.getRarity() != null)
                ps.add(cb.equal(card.get("rarity"), f.getRarity()));
            if (f.getCardCondition() != null)
                ps.add(cb.equal(root.get("cardCondition"), f.getCardCondition()));
            if (f.getMinPrice() != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("price"), f.getMinPrice()));
            if (f.getMaxPrice() != null)
                ps.add(cb.lessThanOrEqualTo(root.get("price"), f.getMaxPrice()));

            // ===== Mỗi thẻ chỉ hiện 1 lần =====
            // Với mỗi thẻ, chỉ giữ sản phẩm có id nhỏ nhất trong các sản phẩm
            // còn thỏa bộ lọc tình trạng và giá
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Product> p2 = sub.from(Product.class);
            List<Predicate> sp = new ArrayList<>();
            sp.add(cb.equal(p2.get("card"), root.get("card")));
            sp.add(cb.isTrue(p2.get("active")));
            if (f.getCardCondition() != null)
                sp.add(cb.equal(p2.get("cardCondition"), f.getCardCondition()));
            if (f.getMinPrice() != null)
                sp.add(cb.greaterThanOrEqualTo(p2.get("price"), f.getMinPrice()));
            if (f.getMaxPrice() != null)
                sp.add(cb.lessThanOrEqualTo(p2.get("price"), f.getMaxPrice()));
            sub.select(cb.min(p2.<Long>get("id")))
                    .where(sp.toArray(new Predicate[0]));

            ps.add(cb.or(
                    cb.isNull(root.get("card")),            // hàng sealed: luôn hiện
                    cb.equal(root.<Long>get("id"), sub)));  // thẻ lẻ: chỉ hiện 1 dòng

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}