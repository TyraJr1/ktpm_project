package com.ktpm.salesmanagement.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.LoginRequest;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.AccountAccessDeniedException;
import com.ktpm.salesmanagement.exception.InvalidCredentialsException;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.security.CustomUserPrincipal;
import com.ktpm.salesmanagement.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    private AuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() {
        User user = user("admin01", "ADMIN", "ACTIVE");
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("valid-password", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.any(CustomUserPrincipal.class)))
                .thenReturn("signed.jwt.token");

        var response = service.login(new LoginRequest("admin01", "valid-password"));

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void loginRejectsUnknownUsernameWithGenericError() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(new LoginRequest("missing", "secret")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    @Test
    void loginRejectsWrongPasswordWithGenericError() {
        User user = user("admin01", "ADMIN", "ACTIVE");
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);
        assertThatThrownBy(() -> service.login(new LoginRequest("admin01", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    @Test
    void loginRejectsInactiveAccountBeforePasswordComparison() {
        User user = user("admin01", "ADMIN", "INACTIVE");
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.login(new LoginRequest("admin01", "secret")))
                .isInstanceOf(AccountAccessDeniedException.class);
    }

    private static User user(String username, String role, String status) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPasswordHash("bcrypt-hash");
        user.setFullName("Demo User");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
