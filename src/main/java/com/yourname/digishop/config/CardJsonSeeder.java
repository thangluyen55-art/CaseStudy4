package com.yourname.digishop.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourname.digishop.entity.*;
import com.yourname.digishop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CardJsonSeeder implements CommandLineRunner {
    private final CardSetRepository sets;
    private final CardRepository cards;
    private final ProductRepository products;
    private final ObjectMapper mapper;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CardImport(String cardCode, String name, String cardType, String color,
                             String rarity, Integer level, Integer playCost, Integer dp,
                             String effect, String setCode, String setName) {}

    @Override
    public void run(String... args) throws Exception {
        var resource = new ClassPathResource("data/cards.json");
        if (!resource.exists()) {
            System.out.println("[CardJsonSeeder] KHONG tim thay data/cards.json");
            return;
        }

        List<CardImport> list;
        try (var in = resource.getInputStream()) {
            list = mapper.readValue(in, new TypeReference<>() {});
        }
        System.out.println("[CardJsonSeeder] Doc duoc " + list.size() + " the tu cards.json");

        double[] factor = {1.0, 0.85, 0.7, 0.5};
        CardCondition[] conds = CardCondition.values();      // NM, LP, MP, HP
        int added = 0;

        for (CardImport d : list) {
            if (cards.findByCardCode(d.cardCode()).isPresent()) continue;   // đã có thì bỏ qua

            CardSet set = sets.findByCode(d.setCode()).orElseGet(() -> {
                CardSet s = new CardSet();
                s.setCode(d.setCode());
                s.setName(d.setName());
                return sets.save(s);
            });

            Card c = new Card();
            c.setCardCode(d.cardCode());
            c.setName(d.name());
            c.setCardSet(set);
            c.setCardType(parse(CardType.class, d.cardType()));
            c.setColor(parse(CardColor.class, d.color()));
            c.setRarity(parse(Rarity.class, d.rarity()));
            c.setLevel(d.level());
            c.setPlayCost(d.playCost());
            c.setDp(d.dp());
            c.setEffect(d.effect() != null && d.effect().length() > 1000
                    ? d.effect().substring(0, 1000) : d.effect());
            cards.save(c);

            BigDecimal base = basePrice(c.getRarity());
            for (int i = 0; i < conds.length; i++) {
                Product p = new Product();
                p.setProductType(ProductType.SINGLE);
                p.setCard(c);
                p.setCardCondition(conds[i]);
                p.setLanguage(Language.EN);
                p.setPrice(base.multiply(BigDecimal.valueOf(factor[i]))
                        .setScale(0, RoundingMode.HALF_UP));
                p.setStock(2 + (int) (Math.random() * 8));          // 2 đến 9
                products.save(p);
            }
            added++;
        }
        System.out.println("[CardJsonSeeder] Da them " + added + " the moi.");
    }

    // Giá gốc theo độ hiếm (số giả định cho demo, tự chỉnh)
    private BigDecimal basePrice(Rarity r) {
        if (r == null) return new BigDecimal("5000");
        return switch (r) {
            case C -> new BigDecimal("5000");
            case U -> new BigDecimal("10000");
            case R -> new BigDecimal("30000");
            case SR -> new BigDecimal("100000");
            case SEC -> new BigDecimal("500000");
            case UR -> new BigDecimal("800000");
        };
    }

    // Đổi chữ thành enum; giá trị lạ thì để trống thay vì báo lỗi
    private <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}