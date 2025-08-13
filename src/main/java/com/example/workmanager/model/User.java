package com.example.workmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    // Manual getters and setters for role
    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private String role = "USER"; // Default role

    @Column(name = "is_system_admin", nullable = false)
    private boolean isSystemAdmin = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("USERROLE")); // mặc định
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override @JsonIgnore public boolean isAccountNonExpired() { return true; }
    @Override @JsonIgnore public boolean isAccountNonLocked() { return true; }
    @Override @JsonIgnore public boolean isCredentialsNonExpired() { return true; }
    @Override @JsonIgnore public boolean isEnabled() { return true; }
}
