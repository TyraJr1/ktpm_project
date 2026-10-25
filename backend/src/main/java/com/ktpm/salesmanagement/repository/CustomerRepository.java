package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

