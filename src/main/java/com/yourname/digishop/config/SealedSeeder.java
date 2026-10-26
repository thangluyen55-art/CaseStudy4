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
import java.util.List;

@Component
@RequiredArgsConstructor
public class SealedSeeder implements CommandLineRunner {
    private final CardSetRepository sets;
    private final ProductRepository products;
    private final ObjectMapper mapper;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SealedImport(String sku, String type, String name, String setCode, String setName,
                               Integer packsPerBox, Integer cardsPerPack, Integer cardsPerDeck,
                               long price, int stock, String image) {}

    @Override
    public void run(String... args) throws Exception {
        var resource = new ClassPathResource("data/sealed.json");
        if (!resource.exists()) return;

        List<SealedImport> list;
        try (var in = resource.getInputStream()) {
            list = mapper.readValue(in, new TypeReference<>() {});
        }

        for (SealedImport d : list) {
            if (products.existsBySku(d.sku())) continue;           // đã có thì bỏ qua

            CardSet set = sets.findByCode(d.setCode()).orElseGet(() -> {
                CardSet s = new CardSet();
                s.setCode(d.setCode());
                s.setName(d.setName());
                return sets.save(s);
            });

            Product p = new Product();
            p.setProductType(ProductType.valueOf(d.type()));
            p.setSku(d.sku());
            p.setName(d.name());
            p.setCardSet(set);
            p.setPacksPerBox(d.packsPerBox());
            p.setCardsPerPack(d.cardsPerPack());
            p.setCardsPerDeck(d.cardsPerDeck());
            p.setLanguage(Language.EN);
            p.setPrice(BigDecimal.valueOf(d.price()));
            p.setStock(d.stock());
            p.setImageUrl(d.image());
            products.save(p);
        }
    }
}