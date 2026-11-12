package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PurchaseReceiptCreateRequest(
        @NotNull(message = "Nhà cung cấp không được để trống")
        Long supplierId,

        @Size(max = 500, message = "Ghi chú phiếu nhập tối đa 500 ký tự")
        String note,

        Boolean confirmImmediately,

        @NotEmpty(message = "Phiếu nhập phải có ít nhất một sản phẩm")
        @Valid
        List<PurchaseReceiptItemRequest> items
) {
}
