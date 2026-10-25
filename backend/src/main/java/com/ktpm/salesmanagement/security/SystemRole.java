package com.ktpm.salesmanagement.security;

import java.util.Arrays;

public enum SystemRole {
    ADMIN,
    SALES_STAFF,
    WAREHOUSE_STAFF;

    public static SystemRole fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported role"));
    }
}
