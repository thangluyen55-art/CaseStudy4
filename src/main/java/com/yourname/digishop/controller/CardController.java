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
import org.springframework.web.bind.annotation.RequestParam;

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

}