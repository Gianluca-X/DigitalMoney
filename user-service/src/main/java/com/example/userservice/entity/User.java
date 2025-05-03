package com.example.userservice.entity;

import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name="user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 50)
    private String dni;  // Cambiado a String

    @Column(length = 50)
    private String phone;  // Cambiado a String

    @Column(length = 50)
    private String email;


    // Atributos generados automáticamente
    @Column(length = 22, unique = true)  // CVU es único y tiene un tamaño fijo de 22 caracteres
    private String cvu;

    @Column(length = 300, unique = true)  // Alias es único y tiene un límite de 100 caracteres
    private String alias;
    @Builder.Default
    private boolean emailVerified = false;
    private String verificationCode;
    @Column(name = "account_id")
    private Long accountId;
    @Enumerated(EnumType.STRING)
    Role role;

    public User() {}
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority((role.name())));
    }

    @Override
    public String getUsername() {
        return email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
