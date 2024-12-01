package com.example.cliniccare.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserInfoDetails implements UserDetails {
    private final UUID userId;
    private final String username;
    private final String password;
    private final String name;
    private final String image;
    private final GrantedAuthority authority;

    public UserInfoDetails(User user) {
        this.userId = user.getUserId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.name = user.getName();
        this.image = user.getImage();
        this.authority = new SimpleGrantedAuthority(user.getRole().getName());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(authority);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
