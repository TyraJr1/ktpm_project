package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SalesOrderCreateRequest(
        Long customerId,

        @Size(max = 30, message = "Phương thức thanh toán tối đa 30 ký tự")
        String paymentMethod,

        @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
        String note,

        @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
        @Valid
        List<SalesOrderItemRequest> items
) {
}
