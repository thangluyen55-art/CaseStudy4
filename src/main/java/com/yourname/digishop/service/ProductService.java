package com.yourname.digishop.service;

import com.yourname.digishop.dto.ProductFilter;
import com.yourname.digishop.entity.Product;
import com.yourname.digishop.repository.ProductRepository;
import com.yourname.digishop.repository.ProductSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    @Transactional(readOnly = true)
    public Page<Product> search(ProductFilter filter, int page) {
        Pageable pageable = PageRequest.of(page, 12, Sort.by("id"));
        return repo.findAll(ProductSpecs.filter(filter), pageable);
    }
}