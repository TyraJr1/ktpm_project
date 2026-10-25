package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.LoginRequest;
import com.ktpm.salesmanagement.dto.response.LoginResponse;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.AccountAccessDeniedException;
import com.ktpm.salesmanagement.exception.InvalidCredentialsException;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.security.CustomUserPrincipal;
import com.ktpm.salesmanagement.security.JwtService;
import com.ktpm.salesmanagement.service.AuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AccountAccessDeniedException("Tài khoản không được phép truy cập.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        final CustomUserPrincipal principal;
        try {
            principal = CustomUserPrincipal.from(user);
        } catch (IllegalArgumentException exception) {
            throw new AccountAccessDeniedException("Tài khoản không được phép truy cập.");
        }
        return new LoginResponse(jwtService.generateToken(principal), "Bearer", user.getId(),
                user.getUsername(), user.getFullName(), principal.getRole().name());
    }
}
