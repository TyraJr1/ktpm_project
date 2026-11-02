package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.response.InvoiceDetailResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceSummaryResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface InvoiceQueryService {
    PageResponse<InvoiceSummaryResponse> search(String code, LocalDate fromDate, LocalDate toDate,
                                                Pageable pageable);
    InvoiceDetailResponse getDetail(Long id);
}
