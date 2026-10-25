package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.LoginRequest;
import com.ktpm.salesmanagement.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
