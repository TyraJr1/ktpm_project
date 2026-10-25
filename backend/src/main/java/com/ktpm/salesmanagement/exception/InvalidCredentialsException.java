package com.ktpm.salesmanagement.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Tên đăng nhập hoặc mật khẩu không chính xác.");
    }
}
