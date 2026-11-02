package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ReportSummaryResponse;
import com.ktpm.salesmanagement.dto.response.TopProductResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {
    ReportSummaryResponse getSummary(LocalDate fromDate, LocalDate toDate);
    List<TopProductResponse> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit);
    PageResponse<InventoryItemResponse> getInventory(Pageable pageable);
}
