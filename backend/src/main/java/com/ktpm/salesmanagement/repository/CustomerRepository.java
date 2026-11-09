package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Customer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findFirstByPhone(String phone);

    @Query("""
            select c from Customer c
            where (:query is null
                or lower(c.customerName) like lower(concat('%', :query, '%'))
                or lower(coalesce(c.phone, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%')))
            """)
    Page<Customer> search(@Param("query") String query, Pageable pageable);
}
