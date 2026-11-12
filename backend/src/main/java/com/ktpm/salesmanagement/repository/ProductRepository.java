package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCodeIgnoreCase(String productCode);

    long countByStatus(String status);

    long countByStockQuantityLessThanEqual(Integer threshold);

    @Query(value = """
            select p from Product p join fetch p.category c
            where (:query is null or lower(p.productCode) like lower(concat('%', :query, '%'))
                    or lower(p.productName) like lower(concat('%', :query, '%')))
              and (:status is null or p.status = :status)
            """,
            countQuery = """
            select count(p) from Product p
            where (:query is null or lower(p.productCode) like lower(concat('%', :query, '%'))
                    or lower(p.productName) like lower(concat('%', :query, '%')))
              and (:status is null or p.status = :status)
            """)
    Page<Product> search(@Param("query") String query, @Param("status") String status,
                         Pageable pageable);
}

