package com.ktpm.salesmanagement.security;

import com.ktpm.salesmanagement.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class CustomUserPrincipal implements UserDetails {
    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final SystemRole role;
    private final boolean active;

    private CustomUserPrincipal(Long userId, String username, String passwordHash,
                                String fullName, SystemRole role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getFullName(), SystemRole.fromDatabaseValue(user.getRole()),
                "ACTIVE".equals(user.getStatus()));
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public SystemRole getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isEnabled() { return active; }
}
