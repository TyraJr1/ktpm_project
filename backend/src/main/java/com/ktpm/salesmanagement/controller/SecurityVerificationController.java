package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.response.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SECURITY VERIFICATION ENDPOINT — not a business API. */
@RestController
@RequestMapping("/api")
public class SecurityVerificationController {
    @GetMapping("/admin/ping")
    public MessageResponse adminPing() { return new MessageResponse("ADMIN access verified"); }

    @GetMapping("/sales/ping")
    public MessageResponse salesPing() { return new MessageResponse("SALES_STAFF access verified"); }

    @GetMapping("/warehouse/ping")
    public MessageResponse warehousePing() { return new MessageResponse("WAREHOUSE_STAFF access verified"); }
}
