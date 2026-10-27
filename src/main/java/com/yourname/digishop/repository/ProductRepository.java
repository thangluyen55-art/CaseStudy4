package com.yourname.digishop.repository;

import com.yourname.digishop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    // Khóa dòng khi trừ kho lúc đặt hàng (dùng ở Sprint 4)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    // Tìm trong trang quản trị: theo tên sản phẩm, tên thẻ hoặc mã thẻ
    @Query("select p from Product p left join p.card c " +
            "where :kw is null " +
            "or lower(p.name) like lower(concat('%', :kw, '%')) " +
            "or lower(c.name) like lower(concat('%', :kw, '%')) " +
            "or lower(c.cardCode) like lower(concat('%', :kw, '%'))")
    Page<Product> adminSearch(@Param("kw") String keyword, Pageable pageable);

    List<Product> findByActiveTrueAndStockLessThanEqualOrderByStockAsc(int stock, Pageable pageable);
    List<Product> findByCardIdAndActiveTrueAndStockGreaterThanOrderByPriceDesc(Long cardId, int stock);
}