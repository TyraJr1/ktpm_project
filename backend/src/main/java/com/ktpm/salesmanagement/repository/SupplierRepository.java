package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCodeIgnoreCase(String supplierCode);

    long countByStatus(String status);

    @Query("""
            select s from Supplier s
            where (:query is null or lower(s.supplierCode) like lower(concat('%', :query, '%'))
                    or lower(s.supplierName) like lower(concat('%', :query, '%')))
              and (:status is null or s.status = :status)
            """)
    Page<Supplier> search(@Param("query") String query, @Param("status") String status,
                          Pageable pageable);
}

