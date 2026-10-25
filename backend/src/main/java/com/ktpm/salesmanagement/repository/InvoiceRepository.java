package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}

