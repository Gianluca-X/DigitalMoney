package com.example.userservice.entity;

import jakarta.persistence.*;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name="user")
public class User  {
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
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = "auth_id")
    private Long authId;
    @Enumerated(EnumType.STRING)
    Role role;

    public User() {}

    public String getEmail() {
        return email;
    }
    public String getUser(){return email;}

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCvu() {
        return cvu;
    }

    public void setCvu(String cvu) {
        this.cvu = cvu;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }
}
