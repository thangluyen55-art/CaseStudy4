package com.yourname.digishop.controller;

import com.yourname.digishop.dto.ProductFilter;
import com.yourname.digishop.entity.*;
import com.yourname.digishop.repository.CardSetRepository;
import com.yourname.digishop.service.ProductService;
import com.yourname.digishop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CardController {
    private final ProductService productService;
    private final CardSetRepository setRepo;
    private final WishlistService wishlistService;

    @GetMapping("/cards")
    public String list(ProductFilter filter,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("page", productService.search(filter, page));
        model.addAttribute("filter", filter);
        model.addAttribute("sets", setRepo.findAll());
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("colors", CardColor.values());
        model.addAttribute("rarities", Rarity.values());
        model.addAttribute("conditions", CardCondition.values());
        return "cards";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);

        // Kiểm tra xem sản phẩm đã được user thêm vào Wishlist chưa
        boolean wishlisted = principal != null && wishlistService.contains(principal.getName(), id);
        model.addAttribute("wishlisted", wishlisted);

        // Nếu là thẻ lẻ, lấy các bản tình trạng khác của cùng 1 thẻ (NM, LP, MP, HP)
        if (product.isSingle() && product.getCard() != null) {
            model.addAttribute("variants", productService.findVariants(product.getCard().getId()));
        } else {
            model.addAttribute("variants", List.of());
        }

        return "card-detail";
    }
}