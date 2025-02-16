package com.DigitalMoneyHouse.accountsservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Accounts")
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long userId;

    private String email;

    private String alias;

    private String cvu;

    private BigDecimal balance;

    private String password; // Campo para almacenar la contraseña del usuario

    @Transient
    private List<Transaction> transactions;

    public Account(Long Id, BigDecimal balance) {
        this.Id = Id;
        this.balance = balance;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(); // Retorna los roles o permisos si los tienes
    }

    @Override
    public String getPassword() {
        return this.password; // Devuelve la contraseña del usuario
    }

    @Override
    public String getUsername() {
        return this.email; // Usa el email como el nombre de usuario
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

