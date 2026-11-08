package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

package com.yourproject.repository;

import com.yourproject.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Tìm khách hàng theo số điện thoại để gán vào đơn hàng mới
    Optional<Customer> findByPhone(String phone);
}