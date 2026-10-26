package com.yourname.digishop.service;

import com.yourname.digishop.entity.Product;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository products;

    private static final Path UPLOAD_DIR = Path.of("uploads/products");
    private static final Map<String, String> EXT = Map.of(
            "image/png", ".png", "image/jpeg", ".jpg", "image/webp", ".webp");

    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, int page) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return products.adminSearch(kw, PageRequest.of(page, 15, Sort.by("id")));
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm #" + id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, BigDecimal price, int stock, boolean active, String name) {
        if (price == null || price.signum() <= 0)
            throw new BusinessException("Giá phải lớn hơn 0");
        if (stock < 0)
            throw new BusinessException("Tồn kho không được âm");

        Product p = get(id);
        p.setPrice(price);
        p.setStock(stock);
        p.setActive(active);
        if (!p.isSingle() && name != null && !name.isBlank())      // chỉ hàng sealed mới đổi tên
            p.setName(name.trim());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveImage(Long id, MultipartFile file) throws IOException {
        Product p = get(id);
        if (p.isSingle())
            throw new BusinessException("Ảnh thẻ lẻ lấy theo mã thẻ trong thư mục ảnh, không tải ở đây");
        if (file == null || file.isEmpty())
            throw new BusinessException("Chưa chọn ảnh");
        String ext = EXT.get(file.getContentType());
        if (ext == null)
            throw new BusinessException("Chỉ nhận ảnh PNG, JPG hoặc WEBP");

        Files.createDirectories(UPLOAD_DIR);
        String filename = UUID.randomUUID() + ext;                 // tên ngẫu nhiên, tránh trùng và tên độc hại
        Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        p.setImageUrl("/uploads/products/" + filename);
    }
}